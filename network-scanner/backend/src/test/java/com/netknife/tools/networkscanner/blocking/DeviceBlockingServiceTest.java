package com.netknife.tools.networkscanner.blocking;

import com.netknife.common.exception.DeviceBlockingException;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.networkscanner.NetworkScannerRepository;
import com.netknife.tools.networkscanner.ScanStateRepository;
import com.netknife.tools.networkscanner.dto.NetworkDeviceDto;
import com.netknife.tools.networkscanner.model.NetworkDevice;
import com.netknife.common.net.GatewayResolver;
import com.netknife.tools.networkscanner.scan.HostDiscoveryService;
import com.netknife.tools.networkscanner.scan.LocalNetworkInfo;
import com.netknife.tools.networkscanner.scan.NetworkRangeDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceBlockingServiceTest {

    private static final String OWN_IP = "192.168.1.10";
    private static final String OWN_MAC = "00:00:00:00:00:01";
    private static final String GATEWAY_IP = "192.168.1.1";
    private static final String GATEWAY_MAC = "00:00:00:00:00:99";

    @Mock
    private NetworkScannerRepository repository;
    @Mock
    private ScanStateRepository scanStateRepository;
    @Mock
    private NetworkRangeDetector rangeDetector;
    @Mock
    private GatewayResolver gatewayResolver;
    @Mock
    private HostDiscoveryService hostDiscoveryService;
    @Mock
    private BlockingCapabilityService capabilityService;
    @Mock
    private DeviceBlockingManager blockingManager;

    private DeviceBlockingService service;

    @BeforeEach
    void setUp() {
        service = new DeviceBlockingService(repository, scanStateRepository, rangeDetector, gatewayResolver,
                hostDiscoveryService, capabilityService, blockingManager);
        lenient().when(capabilityService.refresh())
                .thenReturn(new BlockingCapabilityService.Capability(true, BlockingCapabilityService.Reason.NONE, null));
        lenient().when(scanStateRepository.findById(anyLong())).thenReturn(Optional.empty());
        lenient().when(repository.save(any(NetworkDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /** Los tres primeros datos se necesitan siempre; gateway y su MAC solo si se llega a comprobarlos. */
    private void givenLocalNetworkAndOwnMac() {
        when(rangeDetector.detectLocalNetwork())
                .thenReturn(new LocalNetworkInfo(OWN_IP, (short) 24, OWN_IP + "/24", "Wi-Fi"));
        when(rangeDetector.resolveOwnMacAddress("Wi-Fi")).thenReturn(Optional.of(OWN_MAC));
        lenient().when(gatewayResolver.resolveGatewayIp()).thenReturn(Optional.of(GATEWAY_IP));
        lenient().when(hostDiscoveryService.resolveMacAddress(GATEWAY_IP)).thenReturn(Optional.of(GATEWAY_MAC));
    }

    @Test
    void rejectsBlockingWhenCapabilityIsUnavailable() {
        when(capabilityService.refresh()).thenReturn(new BlockingCapabilityService.Capability(
                false, BlockingCapabilityService.Reason.NPCAP_NOT_INSTALLED, "Falta instalar Npcap"));

        assertThatThrownBy(() -> service.blockDevice(1L))
                .isInstanceOf(DeviceBlockingException.class)
                .hasMessage("Falta instalar Npcap");
        verifyNoInteractions(repository);
    }

    @Test
    void blockingAnUnknownDeviceThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.blockDevice(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsBlockingADeviceWithoutMacAddress() {
        NetworkDevice device = new NetworkDevice(null, "192.168.1.50", null, null, Instant.now());
        when(repository.findById(3L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> service.blockDevice(3L))
                .isInstanceOf(DeviceBlockingException.class)
                .hasMessageContaining("MAC");
        verifyNoInteractions(blockingManager);
    }

    @Test
    void rejectsBlockingTheOwnDevice() {
        givenLocalNetworkAndOwnMac();
        NetworkDevice ownDevice = new NetworkDevice(OWN_MAC, OWN_IP, null, "Dell", Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(ownDevice));

        assertThatThrownBy(() -> service.blockDevice(1L))
                .isInstanceOf(DeviceBlockingException.class)
                .hasMessageContaining("este mismo equipo");
        verify(blockingManager, never()).startBlocking(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsBlockingTheGateway() {
        givenLocalNetworkAndOwnMac();
        NetworkDevice gatewayDevice = new NetworkDevice(GATEWAY_MAC, GATEWAY_IP, null, "TP-Link", Instant.now());
        when(repository.findById(2L)).thenReturn(Optional.of(gatewayDevice));

        assertThatThrownBy(() -> service.blockDevice(2L))
                .isInstanceOf(DeviceBlockingException.class)
                .hasMessageContaining("router");
        verify(blockingManager, never()).startBlocking(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsBlockingWhenTheGatewayCannotBeResolved() {
        givenLocalNetworkAndOwnMac();
        when(gatewayResolver.resolveGatewayIp()).thenReturn(Optional.empty());
        NetworkDevice device = new NetworkDevice("DD:DD:DD:DD:DD:DD", "192.168.1.55", null, "Sony", Instant.now());
        when(repository.findById(7L)).thenReturn(Optional.of(device));

        assertThatThrownBy(() -> service.blockDevice(7L)).isInstanceOf(DeviceBlockingException.class);
        verify(blockingManager, never()).startBlocking(any(), any(), any(), any(), any());
    }

    @Test
    void blockingAnAlreadyBlockedDeviceIsIdempotent() {
        NetworkDevice device = new NetworkDevice("AA:AA:AA:AA:AA:AA", "192.168.1.20", null, "Xiaomi", Instant.now());
        device.setBlocked(true);
        when(repository.findById(4L)).thenReturn(Optional.of(device));

        NetworkDeviceDto dto = service.blockDevice(4L);

        assertThat(dto.blocked()).isTrue();
        verify(blockingManager, never()).startBlocking(any(), any(), any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    void unblockingAnUnknownDeviceThrowsNotFound() {
        when(repository.findById(98L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.unblockDevice(98L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void unblockingANotBlockedDeviceIsIdempotent() {
        NetworkDevice device = new NetworkDevice("BB:BB:BB:BB:BB:BB", "192.168.1.21", null, "LG", Instant.now());
        when(repository.findById(5L)).thenReturn(Optional.of(device));

        NetworkDeviceDto dto = service.unblockDevice(5L);

        assertThat(dto.blocked()).isFalse();
        verify(blockingManager, never()).stopBlocking(any());
        verify(repository, never()).save(any());
    }

    @Test
    void happyPathStartsBlockingAndPersistsState() {
        givenLocalNetworkAndOwnMac();
        NetworkDevice device = new NetworkDevice("CC:CC:CC:CC:CC:CC", "192.168.1.50", null, "Samsung", Instant.now());
        when(repository.findById(6L)).thenReturn(Optional.of(device));

        NetworkDeviceDto dto = service.blockDevice(6L);

        assertThat(dto.blocked()).isTrue();
        assertThat(dto.blockedAt()).isNotNull();

        ArgumentCaptor<HostAddress> victimCaptor = ArgumentCaptor.forClass(HostAddress.class);
        ArgumentCaptor<HostAddress> gatewayCaptor = ArgumentCaptor.forClass(HostAddress.class);
        verify(blockingManager).startBlocking(eq(device.getId()), victimCaptor.capture(), gatewayCaptor.capture(),
                eq(OWN_MAC), eq(OWN_IP));
        assertThat(victimCaptor.getValue()).isEqualTo(new HostAddress("192.168.1.50", "CC:CC:CC:CC:CC:CC"));
        assertThat(gatewayCaptor.getValue()).isEqualTo(new HostAddress(GATEWAY_IP, GATEWAY_MAC));
        assertThat(device.isBlocked()).isTrue();
        assertThat(device.getBlockedAt()).isNotNull();
    }

    @Test
    void happyPathStopsBlockingAndClearsState() {
        NetworkDevice device = new NetworkDevice("EE:EE:EE:EE:EE:EE", "192.168.1.60", null, "Apple", Instant.now());
        device.setBlocked(true);
        device.setBlockedAt(Instant.now());
        when(repository.findById(8L)).thenReturn(Optional.of(device));

        NetworkDeviceDto dto = service.unblockDevice(8L);

        assertThat(dto.blocked()).isFalse();
        assertThat(dto.blockedAt()).isNull();
        verify(blockingManager).stopBlocking(device.getId());
        assertThat(device.isBlocked()).isFalse();
    }

    @Test
    void resetsStaleBlocksLeftFromAPreviousRunOnStartup() {
        NetworkDevice staleBlocked = new NetworkDevice("FF:FF:FF:FF:FF:FF", "192.168.1.70", null, "Google", Instant.now());
        staleBlocked.setBlocked(true);
        staleBlocked.setBlockedAt(Instant.now());
        when(repository.findAll()).thenReturn(List.of(staleBlocked));

        service.resetStaleBlocksOnStartup();

        assertThat(staleBlocked.isBlocked()).isFalse();
        assertThat(staleBlocked.getBlockedAt()).isNull();
        verify(repository).save(staleBlocked);
    }
}
