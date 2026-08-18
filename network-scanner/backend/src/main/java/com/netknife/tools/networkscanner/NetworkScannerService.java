package com.netknife.tools.networkscanner;

import com.netknife.common.dto.RiskLevel;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.networkscanner.dto.DeviceSummaryDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class NetworkScannerService {

    private static final Logger log = LoggerFactory.getLogger(NetworkScannerService.class);

    private final NetworkRangeDetector rangeDetector;
    private final HostDiscoveryService hostDiscoveryService;
    private final OuiLookupService ouiLookupService;
    private final NetworkScannerRepository repository;
    private final ScanStateRepository scanStateRepository;
    private final int maxHostsPerScan;

    public NetworkScannerService(
            NetworkRangeDetector rangeDetector,
            HostDiscoveryService hostDiscoveryService,
            OuiLookupService ouiLookupService,
            NetworkScannerRepository repository,
            ScanStateRepository scanStateRepository,
            @Value("${netknife.scanner.max-hosts-per-scan:254}") int maxHostsPerScan) {
        this.rangeDetector = rangeDetector;
        this.hostDiscoveryService = hostDiscoveryService;
        this.ouiLookupService = ouiLookupService;
        this.repository = repository;
        this.scanStateRepository = scanStateRepository;
        this.maxHostsPerScan = maxHostsPerScan;
    }

    @Transactional
    public ScanResultDto scan() {
        LocalNetworkInfo networkInfo = rangeDetector.detectLocalNetwork();
        List<String> candidateIps = rangeDetector.computeHostAddresses(
                networkInfo.localIpAddress(), networkInfo.prefixLength(), maxHostsPerScan);

        log.info("Escaneando {} direcciones en la subred {} (interfaz: {})",
                candidateIps.size(), networkInfo.cidrNotation(), networkInfo.interfaceName());
        List<DiscoveredHost> discoveredHosts = hostDiscoveryService.discoverHosts(candidateIps);

        Instant now = Instant.now();
        int newDevices = 0;
        for (DiscoveredHost host : discoveredHosts) {
            boolean isNew = mergeDiscoveredHost(host, now);
            if (isNew) {
                newDevices++;
            }
        }
        recordScanCompleted(now);

        List<NetworkDeviceDto> allDevices = listDevices();
        long unrecognized = allDevices.stream().filter(d -> !d.recognized()).count();
        long newSinceLastScan = allDevices.stream().filter(NetworkDeviceDto::newSinceLastScan).count();

        return new ScanResultDto(
                now,
                networkInfo.cidrNotation(),
                networkInfo.interfaceName(),
                allDevices.size(),
                (int) unrecognized,
                newDevices,
                computeRiskLevel((int) unrecognized, (int) newSinceLastScan),
                allDevices
        );
    }

    /**
     * Registra que un escaneo se acaba de completar en el instante {@code now}, para que
     * los dispositivos con firstSeen == now se reconozcan como "nuevos desde el ultimo
     * escaneo" incluso despues de recargar la pantalla sin volver a escanear. Se actualiza
     * siempre, aunque el escaneo no haya encontrado ningun host nuevo, para que los
     * dispositivos nuevos del escaneo anterior dejen de marcarse como "nuevos".
     */
    private void recordScanCompleted(Instant now) {
        ScanState state = scanStateRepository.findById(ScanState.SINGLETON_ID)
                .orElseGet(() -> new ScanState(now));
        state.setLastScanAt(now);
        scanStateRepository.save(state);
    }

    /**
     * Inserta o actualiza un dispositivo descubierto. Devuelve true si es la
     * primera vez que se ve (y por tanto se crea como "no reconocido").
     */
    private boolean mergeDiscoveredHost(DiscoveredHost host, Instant seenAt) {
        if (host.macAddress() == null) {
            // Sin MAC no hay forma fiable de identificar el dispositivo entre escaneos;
            // se ignora para no generar entradas duplicadas sin sentido.
            return false;
        }

        Optional<NetworkDevice> existing = repository.findByMacAddress(host.macAddress());
        String vendor = ouiLookupService.lookupVendor(host.macAddress());

        if (existing.isPresent()) {
            NetworkDevice device = existing.get();
            device.setIpAddress(host.ipAddress());
            device.setLastSeen(seenAt);
            if (host.hostname() != null) {
                device.setHostname(host.hostname());
            }
            if (device.getVendor() == null) {
                device.setVendor(vendor);
            }
            repository.save(device);
            return false;
        }

        NetworkDevice device = new NetworkDevice(host.macAddress(), host.ipAddress(), host.hostname(), vendor, seenAt);
        repository.save(device);
        return true;
    }

    @Transactional(readOnly = true)
    public List<NetworkDeviceDto> listDevices() {
        Instant lastScanAt = currentLastScanAt();
        return repository.findAll().stream()
                .sorted(Comparator.comparing(NetworkDevice::getLastSeen).reversed())
                .map(device -> NetworkDeviceDto.fromEntity(device, lastScanAt))
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceSummaryDto getSummary() {
        List<NetworkDeviceDto> devices = listDevices();
        long unrecognized = devices.stream().filter(d -> !d.recognized()).count();
        long newSinceLastScan = devices.stream().filter(NetworkDeviceDto::newSinceLastScan).count();
        return new DeviceSummaryDto(
                devices.size(),
                (int) unrecognized,
                (int) newSinceLastScan,
                computeRiskLevel((int) unrecognized, (int) newSinceLastScan));
    }

    @Transactional
    public NetworkDeviceDto updateDevice(Long id, UpdateDeviceRequest request) {
        NetworkDevice device = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ningun dispositivo con id " + id));

        if (request.recognized() != null) {
            device.setRecognized(request.recognized());
        }
        if (request.customName() != null) {
            device.setCustomName(request.customName().isBlank() ? null : request.customName().trim());
        }
        repository.save(device);
        return NetworkDeviceDto.fromEntity(device, currentLastScanAt());
    }

    private Instant currentLastScanAt() {
        return scanStateRepository.findById(ScanState.SINGLETON_ID)
                .map(ScanState::getLastScanAt)
                .orElse(null);
    }

    /**
     * El rojo (nuevo desde el ultimo escaneo) tiene prioridad sobre el amarillo (desconocido
     * de antes): un dispositivo nuevo siempre exige revision inmediata, mientras que uno ya
     * visto y aun sin reconocer es una alerta de menor urgencia.
     */
    private RiskLevel computeRiskLevel(int unrecognizedCount, int newSinceLastScanCount) {
        if (newSinceLastScanCount > 0) {
            return RiskLevel.ROJO;
        }
        if (unrecognizedCount > 0) {
            return RiskLevel.AMARILLO;
        }
        return RiskLevel.VERDE;
    }
}
