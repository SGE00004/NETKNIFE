package com.netknife.tools.digitalfootprint.check;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Consulta el registro publico de un dominio (registrador, fechas de alta/
 * caducidad, servidores DNS) via RDAP (rdap.org), el sucesor moderno y
 * estandarizado en JSON del protocolo WHOIS clasico. Como es la primera
 * llamada saliente a internet de toda la app, cualquier fallo (timeout, DNS,
 * dominio sin registrar) se traduce en Optional.empty() en vez de propagar
 * la excepcion: no bloquea el resto del informe.
 */
@Component
public class DomainWhoisLookup {

    private static final Logger log = LoggerFactory.getLogger(DomainWhoisLookup.class);

    private final RestClient restClient;

    public DomainWhoisLookup(@Value("${netknife.digital-footprint.http-timeout-ms:5000}") int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public Optional<WhoisSummary> lookup(String domain) {
        try {
            JsonNode root = restClient.get()
                    .uri("https://rdap.org/domain/{domain}", domain)
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null) {
                return Optional.empty();
            }
            return Optional.of(new WhoisSummary(
                    findRegistrar(root),
                    findEventDate(root, "registration"),
                    findEventDate(root, "expiration"),
                    findNameservers(root)));
        } catch (Exception e) {
            log.debug("No se pudo consultar RDAP para {}: {}", domain, e.getMessage());
            return Optional.empty();
        }
    }

    private String findRegistrar(JsonNode root) {
        for (JsonNode entity : root.path("entities")) {
            boolean isRegistrar = false;
            for (JsonNode role : entity.path("roles")) {
                if ("registrar".equalsIgnoreCase(role.asText())) {
                    isRegistrar = true;
                }
            }
            if (!isRegistrar) {
                continue;
            }
            String fromVcard = findVcardFn(entity.path("vcardArray"));
            if (fromVcard != null) {
                return fromVcard;
            }
            String handle = entity.path("handle").asText(null);
            if (handle != null) {
                return handle;
            }
        }
        return null;
    }

    private String findVcardFn(JsonNode vcardArray) {
        if (!vcardArray.isArray() || vcardArray.size() < 2) {
            return null;
        }
        for (JsonNode property : vcardArray.get(1)) {
            if (property.isArray() && property.size() >= 4 && "fn".equalsIgnoreCase(property.get(0).asText())) {
                return property.get(3).asText(null);
            }
        }
        return null;
    }

    private String findEventDate(JsonNode root, String eventAction) {
        for (JsonNode event : root.path("events")) {
            if (eventAction.equalsIgnoreCase(event.path("eventAction").asText())) {
                return event.path("eventDate").asText(null);
            }
        }
        return null;
    }

    private List<String> findNameservers(JsonNode root) {
        List<String> nameservers = new ArrayList<>();
        for (JsonNode ns : root.path("nameservers")) {
            String name = ns.path("ldhName").asText(null);
            if (name != null) {
                nameservers.add(name);
            }
        }
        return nameservers;
    }
}
