package com.netknife.tools.digitalfootprint.check;

import com.netknife.common.dto.CheckStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Descubre subdominios probando a resolver una lista curada de prefijos
 * habituales contra el dominio indicado, en vez de una fuerza bruta
 * exhaustiva: es rapido y cubre los casos mas comunes de exposicion
 * accidental (paneles de admin, entornos de desarrollo olvidados, etc).
 * InetAddress.getByName no soporta timeout nativo, asi que cada resolucion
 * se lanza en su propio hilo con un timeout explicito via Future.
 */
@Component
public class SubdomainDiscoveryService {

    private final int resolveTimeoutMs;
    private final int concurrency;

    public SubdomainDiscoveryService(
            @Value("${netknife.digital-footprint.subdomain-resolve-timeout-ms:1500}") int resolveTimeoutMs,
            @Value("${netknife.digital-footprint.subdomain-concurrency:20}") int concurrency) {
        this.resolveTimeoutMs = resolveTimeoutMs;
        this.concurrency = concurrency;
    }

    public List<DiscoveredSubdomain> discover(String domain) {
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        try {
            List<Future<Optional<DiscoveredSubdomain>>> futures = CuratedSubdomainWordlist.PREFIXES.stream()
                    .map(prefix -> executor.submit(() -> resolve(prefix, domain)))
                    .toList();

            return futures.stream()
                    .map(this::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted((a, b) -> a.subdomain().compareTo(b.subdomain()))
                    .collect(Collectors.toList());
        } finally {
            executor.shutdown();
        }
    }

    private Optional<DiscoveredSubdomain> get(Future<Optional<DiscoveredSubdomain>> future) {
        try {
            return future.get(resolveTimeoutMs + 500L, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<DiscoveredSubdomain> resolve(String prefix, String domain) {
        String hostname = prefix + "." + domain;
        try {
            InetAddress address = InetAddress.getByName(hostname);
            CheckStatus status = CuratedSubdomainWordlist.SENSITIVE_PREFIXES.contains(prefix)
                    ? CheckStatus.ATENCION
                    : CheckStatus.OK;
            return Optional.of(new DiscoveredSubdomain(hostname, address.getHostAddress(), status));
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }
}
