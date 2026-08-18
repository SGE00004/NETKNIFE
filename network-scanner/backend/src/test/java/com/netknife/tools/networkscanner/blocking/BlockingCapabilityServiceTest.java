package com.netknife.tools.networkscanner.blocking;

import com.netknife.common.system.AdminPrivilegeChecker;
import com.netknife.common.system.NpcapInstallationChecker;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * El bloqueo de dispositivos solo esta soportado en Windows (primera comprobacion
 * de {@link BlockingCapabilityService#detect}), asi que estos tests solo tienen
 * sentido ejecutandose en Windows; en otro SO se saltan en vez de fallar en falso.
 * La ultima comprobacion (inicializacion real de pcap4j) depende de si Npcap esta
 * genuinamente instalado en la maquina, asi que no se testea aqui de forma
 * deterministica: queda cubierta por las pruebas manuales.
 */
@ExtendWith(MockitoExtension.class)
class BlockingCapabilityServiceTest {

    @Mock
    private AdminPrivilegeChecker adminPrivilegeChecker;
    @Mock
    private NpcapInstallationChecker npcapInstallationChecker;

    private BlockingCapabilityService service;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(
                System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win"),
                "El bloqueo de dispositivos solo se soporta en Windows");
        service = new BlockingCapabilityService(adminPrivilegeChecker, npcapInstallationChecker);
    }

    @Test
    void unavailableWhenMissingAdministratorPrivileges() {
        when(adminPrivilegeChecker.hasAdministratorPrivileges()).thenReturn(false);

        BlockingCapabilityService.Capability capability = service.refresh();

        assertThat(capability.available()).isFalse();
        assertThat(capability.reason()).isEqualTo(BlockingCapabilityService.Reason.MISSING_ADMIN_PRIVILEGES);
        assertThat(capability.message()).isNotBlank();
        verifyNoInteractions(npcapInstallationChecker);
    }

    @Test
    void unavailableWhenNpcapIsNotInstalled() {
        when(adminPrivilegeChecker.hasAdministratorPrivileges()).thenReturn(true);
        when(npcapInstallationChecker.isInstalled()).thenReturn(false);

        BlockingCapabilityService.Capability capability = service.refresh();

        assertThat(capability.available()).isFalse();
        assertThat(capability.reason()).isEqualTo(BlockingCapabilityService.Reason.NPCAP_NOT_INSTALLED);
        assertThat(capability.message()).isNotBlank();
    }

    @Test
    void currentReturnsTheValueCachedAtStartupWithoutRecomputing() {
        when(adminPrivilegeChecker.hasAdministratorPrivileges()).thenReturn(false);

        service.refresh();
        BlockingCapabilityService.Capability cached = service.current();

        assertThat(cached.available()).isFalse();
        assertThat(cached.reason()).isEqualTo(BlockingCapabilityService.Reason.MISSING_ADMIN_PRIVILEGES);
    }
}
