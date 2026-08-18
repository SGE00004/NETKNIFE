package com.netknife.tools.networkscanner.scan;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Estima el fabricante de un dispositivo a partir del prefijo OUI de su MAC
 * (los primeros 3 octetos), usando una tabla local embebida como recurso
 * (oui-table.csv). No se realiza ninguna llamada a servicios externos.
 */
@Component
public class OuiLookupService {

    private static final Logger log = LoggerFactory.getLogger(OuiLookupService.class);
    private static final String DESCONOCIDO = "Fabricante desconocido";

    private final Map<String, String> ouiTable = new HashMap<>();

    @PostConstruct
    void loadTable() {
        ClassPathResource resource = new ClassPathResource("oui-table.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int comma = line.indexOf(',');
                if (comma < 0) {
                    continue;
                }
                String prefix = normalizePrefix(line.substring(0, comma));
                String vendor = line.substring(comma + 1).trim();
                ouiTable.put(prefix, vendor);
            }
            log.info("Tabla OUI local cargada con {} fabricantes", ouiTable.size());
        } catch (IOException e) {
            log.error("No se pudo cargar la tabla OUI local, la deteccion de fabricante quedara deshabilitada", e);
        }
    }

    public String lookupVendor(String macAddress) {
        if (macAddress == null || macAddress.isBlank()) {
            return DESCONOCIDO;
        }
        String normalized = macAddress.toUpperCase(Locale.ROOT).replace('-', ':');
        String[] octets = normalized.split(":");
        if (octets.length < 3) {
            return DESCONOCIDO;
        }
        String prefix = octets[0] + ":" + octets[1] + ":" + octets[2];
        return ouiTable.getOrDefault(prefix, DESCONOCIDO);
    }

    private String normalizePrefix(String prefix) {
        return prefix.trim().toUpperCase(Locale.ROOT).replace('-', ':');
    }
}
