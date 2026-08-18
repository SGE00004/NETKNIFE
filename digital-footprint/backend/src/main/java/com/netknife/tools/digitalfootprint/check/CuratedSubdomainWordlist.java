package com.netknife.tools.digitalfootprint.check;

import java.util.Set;

/**
 * Lista curada de prefijos de subdominio habituales, para un descubrimiento
 * rapido (resolucion DNS directa, sin fuerza bruta exhaustiva). Los prefijos
 * marcados como "sensibles" (paneles de administracion, entornos de
 * desarrollo/pruebas, copias de seguridad...) suben el riesgo si resuelven,
 * porque no deberian ser publicos ni faciles de encontrar.
 */
public final class CuratedSubdomainWordlist {

    public static final Set<String> PREFIXES = Set.of(
            "www", "www2", "mail", "mail2", "webmail", "smtp", "smtp2", "pop", "imap",
            "ftp", "ftp2", "sftp", "admin", "administrator", "api", "api2", "dev", "dev2",
            "staging", "stage", "test", "test2", "uat", "qa", "vpn", "vpn2", "remote",
            "portal", "secure", "login", "sso", "auth", "cpanel", "whm", "autodiscover",
            "ns1", "ns2", "ns3", "mx", "mx1", "cdn", "static", "assets", "media", "images",
            "blog", "news", "shop", "store", "m", "mobile", "app", "apps", "docs", "wiki",
            "help", "support", "status", "monitor", "git", "gitlab", "jenkins", "ci", "db",
            "database", "mysql", "backup", "backups", "old", "legacy", "beta", "demo",
            "sandbox", "intranet", "internal", "owa", "exchange", "direct", "connect", "preprod"
    );

    public static final Set<String> SENSITIVE_PREFIXES = Set.of(
            "admin", "administrator", "cpanel", "whm", "vpn", "vpn2", "staging", "stage",
            "dev", "dev2", "test", "test2", "uat", "qa", "backup", "backups", "git",
            "gitlab", "jenkins", "ci", "db", "database", "mysql", "internal", "intranet",
            "sso", "auth", "sandbox", "preprod", "legacy", "old", "remote"
    );

    private CuratedSubdomainWordlist() {
    }
}
