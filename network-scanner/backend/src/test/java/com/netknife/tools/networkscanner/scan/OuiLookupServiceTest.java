package com.netknife.tools.networkscanner.scan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OuiLookupServiceTest {

    private OuiLookupService service;

    @BeforeEach
    void setUp() {
        service = new OuiLookupService();
        service.loadTable();
    }

    @Test
    void findsKnownVendorByPrefix() {
        assertThat(service.lookupVendor("B8:27:EB:AA:BB:CC")).isEqualTo("Raspberry Pi Foundation");
    }

    @Test
    void lookupIsCaseAndSeparatorInsensitive() {
        assertThat(service.lookupVendor("b8-27-eb-aa-bb-cc")).isEqualTo("Raspberry Pi Foundation");
    }

    @Test
    void returnsUnknownForUnrecognizedPrefix() {
        assertThat(service.lookupVendor("FF:FF:FF:FF:FF:FF")).isEqualTo("Fabricante desconocido");
    }

    @Test
    void returnsUnknownForNullMac() {
        assertThat(service.lookupVendor(null)).isEqualTo("Fabricante desconocido");
    }
}
