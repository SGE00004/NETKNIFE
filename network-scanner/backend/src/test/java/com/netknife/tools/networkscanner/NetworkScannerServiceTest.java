package com.netknife.tools.networkscanner;

import com.netknife.common.dto.RiskLevel;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.networkscanner.dto.NetworkDeviceDto;
import com.netknife.tools.networkscanner.dto.ScanResultDto;
import com.netknife.tools.networkscanner.dto.UpdateDeviceRequest;
import com.netknife.tools.networkscanner.model.NetworkDevice;
import com.netknife.tools.networkscanner.model.ScanState;
import com.netknife.tools.networkscanner.scan.DiscoveredHost;
import com.netknife.tools.networkscanner.scan.HostDiscoveryService;
import com.netknife.tools.networkscanner.scan.LocalNetworkInfo;
import com.netknife.tools.networkscanner.scan.NetworkRangeDetector;
import com.netknife.tools.networkscanner.scan.OuiLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkScannerServiceTest {

    @Mock
    private NetworkRangeDetector rangeDetector;
    @Mock
    private HostDiscoveryService hostDiscoveryService;
    @Mock
    private OuiLookupService ouiLookupService;
    @Mock
    private NetworkScannerRepository repository;
    @Mock
    private ScanStateRepository scanStateRepository;

    private NetworkScannerService service;

    /**
     * Los mocks de Mockito no recuerdan por si solos lo que se les "guarda"; para probar
     * recordScanCompleted() -> listDevices()/getSummary() dentro del mismo scan() hace falta
     * que save() y findById() compartan estado, como haria una tabla real de una sola fila.
     */
    private ScanState[] persistedScanState;

    @BeforeEach
    void setUp() {
        service = new NetworkScannerService(
                rangeDetector, hostDiscoveryService, ouiLookupService, repository, scanStateRepository, 254);
        persistedScanState = new ScanState[1];
        lenient().when(scanStateRepository.findById(ScanState.SINGLETON_ID))
                .thenAnswer(invocation -> Optional.ofNullable(persistedScanState[0]));
        lenient().when(scanStateRepository.save(any(ScanState.class))).thenAnswer(invocation -> {
            ScanState saved = invocation.getArgument(0);
            persistedScanState[0] = saved;
            return saved;
        });
    }

    private void givenLocalNetwork(List<String> candidateIps) {
        when(rangeDetector.detectLocalNetwork())
                .thenReturn(new LocalNetworkInfo("192.168.1.10", (short) 24, "192.168.1.10/24", "Wi-Fi"));
        when(rangeDetector.computeHostAddresses(anyString(), anyShort(), anyInt())).thenReturn(candidateIps);
    }

    @Test
    void aBrandNewDeviceIsMarkedUnrecognizedAndNewSinceLastScan() {
        givenLocalNetwork(List.of("192.168.1.20"));
        when(hostDiscoveryService.discoverHosts(any()))
                .thenReturn(List.of(new DiscoveredHost("192.168.1.20", "AA:BB:CC:DD:EE:FF", "salon-tv")));
        when(ouiLookupService.lookupVendor("AA:BB:CC:DD:EE:FF")).thenReturn("Samsung");
        when(repository.findByMacAddress("AA:BB:CC:DD:EE:FF")).thenReturn(Optional.empty());
        // findAll() debe reflejar el dispositivo realmente creado por el merge (con el mismo
        // "now" exacto que scanState), no uno independiente con su propio Instant.now(): dos
        // llamadas a Instant.now() nunca son bit a bit iguales, lo que haria fallar newSinceLastScan.
        NetworkDevice[] savedDevice = new NetworkDevice[1];
        when(repository.save(any(NetworkDevice.class))).thenAnswer(invocation -> {
            savedDevice[0] = invocation.getArgument(0);
            return savedDevice[0];
        });
        when(repository.findAll()).thenAnswer(invocation -> List.of(savedDevice[0]));

        ScanResultDto result = service.scan();

        assertThat(result.newDevicesThisScan()).isEqualTo(1);
        assertThat(result.unrecognizedDevices()).isEqualTo(1);
        // Un dispositivo nunca visto antes debe disparar el nivel de alerta mas alto.
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.ROJO);
        assertThat(result.devices()).hasSize(1);
        assertThat(result.devices().get(0).newSinceLastScan()).isTrue();

        ArgumentCaptor<NetworkDevice> captor = ArgumentCaptor.forClass(NetworkDevice.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isRecognized()).isFalse();
    }

    @Test
    void riskIsGreenWhenAllDevicesAreRecognized() {
        givenLocalNetwork(List.of());
        when(hostDiscoveryService.discoverHosts(any())).thenReturn(List.of());

        NetworkDevice recognizedDevice = new NetworkDevice("11:22:33:44:55:66", "192.168.1.5", "pc-ana", "Dell", Instant.now());
        recognizedDevice.setRecognized(true);
        when(repository.findAll()).thenReturn(List.of(recognizedDevice));

        ScanResultDto result = service.scan();

        assertThat(result.unrecognizedDevices()).isZero();
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.VERDE);
    }

    @Test
    void aDeviceSeenBeforeAndStillUnrecognizedIsNotFlaggedAsNewOnASubsequentScan() {
        // El dispositivo ya existia en la base de datos (visto en un escaneo anterior) y sigue
        // sin reconocerse; en este escaneo vuelve a detectarse pero no debe contar como "nuevo".
        Instant firstScan = Instant.now().minus(2, ChronoUnit.DAYS);

        givenLocalNetwork(List.of("192.168.1.30"));
        when(hostDiscoveryService.discoverHosts(any()))
                .thenReturn(List.of(new DiscoveredHost("192.168.1.30", "AA:AA:AA:AA:AA:AA", null)));
        when(ouiLookupService.lookupVendor("AA:AA:AA:AA:AA:AA")).thenReturn("TP-Link");

        NetworkDevice existingDevice = new NetworkDevice("AA:AA:AA:AA:AA:AA", "192.168.1.30", null, "TP-Link", firstScan);
        when(repository.findByMacAddress("AA:AA:AA:AA:AA:AA")).thenReturn(Optional.of(existingDevice));
        when(repository.findAll()).thenReturn(List.of(existingDevice));

        ScanResultDto result = service.scan();

        assertThat(result.newDevicesThisScan()).isZero();
        assertThat(result.unrecognizedDevices()).isEqualTo(1);
        assertThat(result.riskLevel()).isEqualTo(RiskLevel.AMARILLO);
        assertThat(result.devices().get(0).newSinceLastScan()).isFalse();
        // El dispositivo existente se actualiza (lastSeen), nunca se vuelve a crear.
        verify(repository, org.mockito.Mockito.never()).save(org.mockito.ArgumentMatchers.argThat(
                d -> d != existingDevice));
        assertThat(existingDevice.getLastSeen()).isAfter(firstScan);
        assertThat(existingDevice.getFirstSeen()).isEqualTo(firstScan);
    }

    @Test
    void aRecognizedDeviceNeverCountsAsNewEvenIfFirstSeenMatchesTheLastScan() {
        // Si el usuario reconoce un dispositivo, deja de generar alerta aunque su firstSeen
        // coincida con el instante del ultimo escaneo (p.ej. lo reconocio justo tras detectarlo).
        Instant lastScanAt = Instant.now();
        NetworkDevice recognized = new NetworkDevice("BB:BB:BB:BB:BB:BB", "192.168.1.40", null, "Apple", lastScanAt);
        recognized.setRecognized(true);

        when(scanStateRepository.findById(ScanState.SINGLETON_ID))
                .thenReturn(Optional.of(new ScanState(lastScanAt)));
        when(repository.findAll()).thenReturn(List.of(recognized));

        List<NetworkDeviceDto> devices = service.listDevices();

        assertThat(devices).hasSize(1);
        assertThat(devices.get(0).recognized()).isTrue();
        assertThat(devices.get(0).newSinceLastScan()).isFalse();
    }

    @Test
    void summaryReflectsThreeTierRiskAcrossMixedDevices() {
        Instant lastScanAt = Instant.now();
        Instant longAgo = lastScanAt.minus(10, ChronoUnit.DAYS);

        NetworkDevice recognized = new NetworkDevice("11:11:11:11:11:11", "192.168.1.2", null, "Dell", longAgo);
        recognized.setRecognized(true);
        NetworkDevice knownUnrecognized = new NetworkDevice("22:22:22:22:22:22", "192.168.1.3", null, "TP-Link", longAgo);
        NetworkDevice brandNew = new NetworkDevice("33:33:33:33:33:33", "192.168.1.4", null, "Samsung", lastScanAt);

        when(scanStateRepository.findById(ScanState.SINGLETON_ID))
                .thenReturn(Optional.of(new ScanState(lastScanAt)));
        when(repository.findAll()).thenReturn(List.of(recognized, knownUnrecognized, brandNew));

        var summary = service.getSummary();

        assertThat(summary.totalDevices()).isEqualTo(3);
        assertThat(summary.unrecognizedDevices()).isEqualTo(2);
        assertThat(summary.newSinceLastScan()).isEqualTo(1);
        assertThat(summary.riskLevel()).isEqualTo(RiskLevel.ROJO);
    }

    @Test
    void markingANewDeviceAsRecognizedClearsItsNewFlagImmediately() {
        Instant lastScanAt = Instant.now();
        NetworkDevice brandNew = new NetworkDevice("44:44:44:44:44:44", "192.168.1.50", null, "Xiaomi", lastScanAt);

        when(repository.findById(5L)).thenReturn(Optional.of(brandNew));
        when(scanStateRepository.findById(ScanState.SINGLETON_ID))
                .thenReturn(Optional.of(new ScanState(lastScanAt)));

        NetworkDeviceDto dto = service.updateDevice(5L, new UpdateDeviceRequest(true, null));

        assertThat(dto.recognized()).isTrue();
        assertThat(dto.newSinceLastScan()).isFalse();
    }

    @Test
    void updatingAnUnknownDeviceThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateDevice(99L, new UpdateDeviceRequest(true, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDevicePersistsRecognizedFlagAndCustomName() {
        NetworkDevice device = new NetworkDevice("AA:AA:AA:AA:AA:AA", "192.168.1.30", null, "TP-Link", Instant.now());
        when(repository.findById(1L)).thenReturn(Optional.of(device));

        NetworkDeviceDto dto = service.updateDevice(1L, new UpdateDeviceRequest(true, "Router del salon"));

        assertThat(dto.recognized()).isTrue();
        assertThat(dto.customName()).isEqualTo("Router del salon");
        assertThat(dto.displayName()).isEqualTo("Router del salon");
        verify(repository).save(device);
    }

    @Test
    void unmarkingADeviceRevertsItToUnrecognized() {
        NetworkDevice device = new NetworkDevice("CC:CC:CC:CC:CC:CC", "192.168.1.60", null, "LG", Instant.now());
        device.setRecognized(true);
        when(repository.findById(2L)).thenReturn(Optional.of(device));

        NetworkDeviceDto dto = service.updateDevice(2L, new UpdateDeviceRequest(false, null));

        assertThat(dto.recognized()).isFalse();
        verify(repository).save(device);
    }
}
