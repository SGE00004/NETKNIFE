package com.netknife.tools.wifirouteraudit.check;

import java.util.List;

/**
 * Pares usuario/contrasena de fabrica mas habituales en paneles de router
 * domesticos, para probar contra un panel que use autenticacion HTTP Basic.
 * Separada de DefaultCredentialsCheck para poder testear con un subconjunto
 * sin depender de la lista completa.
 */
public final class CommonRouterCredentials {

    public record Credential(String username, String password) {
    }

    public static final List<Credential> ALL = List.of(
            new Credential("admin", "admin"),
            new Credential("admin", "password"),
            new Credential("admin", "1234"),
            new Credential("admin", ""),
            new Credential("admin", "admin1234"),
            new Credential("admin", "12345"),
            new Credential("root", "root"),
            new Credential("root", "admin"),
            new Credential("user", "user"),
            new Credential("admin", "telecom"),
            new Credential("admin", "motorola"),
            new Credential("admin", "zte521"),
            new Credential("cisco", "cisco"),
            new Credential("admin", "epicrouter"),
            new Credential("admin", "changeme"),
            new Credential("support", "support"),
            new Credential("admin", "1234567890")
    );

    private CommonRouterCredentials() {
    }
}
