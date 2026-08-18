package com.netknife.tools.wifirouteraudit.dto;

/** routerAddress es opcional: si viene vacio, el servicio intenta autodetectar el gateway. */
public record RunAuditRequest(String routerAddress) {
}
