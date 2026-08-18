package com.netknife.common.geo;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resuelve la IP publica de un host (dominio o IP) y consulta su geolocalizacion
 * aproximada (pais, ciudad, proveedor de internet) via ip-api.com, un servicio
 * gratuito sin necesidad de clave de API. Cualquier fallo se traduce en
 * Optional.empty(): esta comprobacion es informativa, nunca debe bloquear al
 * modulo que la usa.
 *
 * Vive en platform/common (en vez de en digital-footprint, su modulo de origen)
 * porque traffic-analyzer tambien la necesita para geolocalizar cada conexion
 * activa en cada ciclo de polling. Por eso se anadio una cache en memoria por IP:
 * ip-api.com limita a 45 peticiones/minuto sin clave, y sin cache un puñado de
 * conexiones repitiendose en cada poll de 2-3s la agotaria enseguida. Los fallos
 * tambien se cachean (como Optional.empty()) para no reintentar en cada poll una
 * IP que ya fallo. Limitacion conocida y aceptada: la cache no tiene limite de
 * tamaño ni expulsion activa, adecuado para una app de escritorio de sesiones
 * acotadas, no para un servicio de larga duracion.
 */
@Component
public class IpGeolocationLookup {

    private static final Logger log = LoggerFactory.getLogger(IpGeolocationLookup.class);

    private record CacheEntry(Optional<GeoLocation> result, Instant fetchedAt) {
        boolean isExpired(Duration ttl) {
            return Duration.between(fetchedAt, Instant.now()).compareTo(ttl) > 0;
        }
    }

    private final RestClient restClient;
    private final Duration cacheTtl;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public IpGeolocationLookup(
            @Value("${netknife.common.geo.http-timeout-ms:5000}") int timeoutMs,
            @Value("${netknife.common.geo.cache-ttl-minutes:60}") long cacheTtlMinutes) {
        this.cacheTtl = Duration.ofMinutes(cacheTtlMinutes);
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    /** Acepta tanto un dominio como una direccion IP: internamente ambos se resuelven igual. */
    public Optional<GeoLocation> lookup(String host) {
        String ip;
        try {
            ip = InetAddress.getByName(host).getHostAddress();
        } catch (UnknownHostException e) {
            log.debug("No se pudo resolver {}: {}", host, e.getMessage());
            return Optional.empty();
        }

        CacheEntry cached = cache.get(ip);
        if (cached != null && !cached.isExpired(cacheTtl)) {
            return cached.result();
        }

        Optional<GeoLocation> result = fetchFromApi(ip);
        cache.put(ip, new CacheEntry(result, Instant.now()));
        return result;
    }

    private Optional<GeoLocation> fetchFromApi(String ip) {
        try {
            JsonNode root = restClient.get()
                    .uri("http://ip-api.com/json/{ip}?fields=status,country,regionName,city,isp,org,lat,lon,query", ip)
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null || !"success".equalsIgnoreCase(root.path("status").asText())) {
                return Optional.empty();
            }
            return Optional.of(new GeoLocation(
                    ip,
                    root.path("country").asText(null),
                    root.path("city").asText(null),
                    root.path("isp").asText(null),
                    root.path("org").asText(null),
                    root.path("lat").isMissingNode() ? null : root.path("lat").asDouble(),
                    root.path("lon").isMissingNode() ? null : root.path("lon").asDouble()));
        } catch (Exception e) {
            log.debug("No se pudo geolocalizar {}: {}", ip, e.getMessage());
            return Optional.empty();
        }
    }
}
