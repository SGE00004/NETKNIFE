package com.netknife.tools.networkscanner.blocking;

import com.netknife.common.exception.DeviceBlockingException;
import com.netknife.common.exception.ResourceNotFoundException;
import com.netknife.tools.networkscanner.NetworkScannerRepository;
import com.netknife.tools.networkscanner.ScanStateRepository;
import com.netknife.tools.networkscanner.blocking.BlockingCapabilityService.Capability;
import com.netknife.tools.networkscanner.blocking.dto.BlockingCapabilityDto;
import com.netknife.tools.networkscanner.dto.NetworkDeviceDto;
import com.netknife.tools.networkscanner.model.NetworkDevice;
import com.netknife.tools.networkscanner.model.ScanState;
import com.netknife.common.net.GatewayResolver;
import com.netknife.tools.networkscanner.scan.HostDiscoveryService;
import com.netknife.tools.networkscanner.scan.LocalNetworkInfo;
import com.netknife.tools.networkscanner.scan.NetworkRangeDetector;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Orquesta el bloqueo/desbloqueo de un dispositivo: resuelve la red local, el
 * gateway y sus MACs, aplica los guardarrailes de seguridad (nunca bloquear el
 * propio equipo ni el router) y delega el envenenamiento ARP en si a
 * {@link DeviceBlockingManager}. Es el unico punto de esta funcionalidad que toca
 * el controller.
 */
@Service
public class DeviceBlockingService {

    private static final Logger log = LoggerFactory.getLogger(DeviceBlockingService.class);

    private final NetworkScannerRepository repository;
    private final ScanStateRepository scanStateRepository;
    private final NetworkRangeDetector rangeDetector;
    private final GatewayResolver gatewayResolver;
    private final HostDiscoveryService hostDiscoveryService;
    private final BlockingCapabilityService capabilityService;
    private final DeviceBlockingManager blockingManager;

    public DeviceBlockingService(
            NetworkScannerRepository repository,
            ScanStateRepository scanStateRepository,
            NetworkRangeDetector rangeDetector,
            GatewayResolver gatewayResolver,
            HostDiscoveryService hostDiscoveryService,
            BlockingCapabilityService capabilityService,
            DeviceBlockingManager blockingManager) {
        this.repository = repository;
        this.scanStateRepository = scanStateRepository;
        this.rangeDetector = rangeDetector;
        this.gatewayResolver = gatewayResolver;
        this.hostDiscoveryService = hostDiscoveryService;
        this.capabilityService = capabilityService;
        this.blockingManager = blockingManager;
    }

    /**
     * Si la app se cerro de forma inesperada (crash, corte de luz...) con algun
     * dispositivo bloqueado, ese estado sobrevive en la base de datos pero ya no
     * hay ninguna tarea de envenenamiento activa en esta nueva ejecucion (el
     * gestor de bloqueos solo vive en memoria). Se refleja eso en lugar de dejar
     * al dispositivo "atascado" como bloqueado en la UI sin forma de desbloquearlo.
     */
    @PostConstruct
    void resetStaleBlocksOnStartup() {
        List<NetworkDevice> staleBlocked = repository.findAll().stream()
                .filter(NetworkDevice::isBlocked)
                .toList();
        if (staleBlocked.isEmpty()) {
            return;
        }
        log.warn("{} dispositivo(s) seguian marcados como bloqueados de una ejecucion anterior; se restablecen "
                + "(la conectividad ya se habra recuperado sola o al reiniciar)", staleBlocked.size());
        for (NetworkDevice device : staleBlocked) {
            device.setBlocked(false);
            device.setBlockedAt(null);
            repository.save(device);
        }
    }

    public BlockingCapabilityDto getCapability() {
        return BlockingCapabilityDto.fromCapability(capabilityService.current());
    }

    @Transactional
    public NetworkDeviceDto blockDevice(Long id) {
        Capability capability = capabilityService.refresh();
        if (!capability.available()) {
            throw new DeviceBlockingException(capability.message());
        }

        NetworkDevice device = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ningun dispositivo con id " + id));
        if (device.isBlocked()) {
            return toDto(device);
        }
        if (device.getMacAddress() == null) {
            throw new DeviceBlockingException(
                    "No se puede bloquear un dispositivo sin direccion MAC conocida. Vuelve a escanear la red.");
        }

        LocalNetworkInfo net = rangeDetector.detectLocalNetwork();
        String ownIp = net.localIpAddress();
        String ownMac = rangeDetector.resolveOwnMacAddress(net.interfaceName())
                .orElseThrow(() -> new DeviceBlockingException("No se ha podido determinar la direccion MAC de este equipo."));

        if (device.getIpAddress().equals(ownIp) || device.getMacAddress().equalsIgnoreCase(ownMac)) {
            throw new DeviceBlockingException("No puedes bloquear este mismo equipo: es donde se ejecuta NETKNIFE.");
        }

        String gatewayIp = gatewayResolver.resolveGatewayIp()
                .orElseThrow(() -> new DeviceBlockingException("No se ha podido determinar el router de tu red."));
        String gatewayMac = hostDiscoveryService.resolveMacAddress(gatewayIp)
                .orElseThrow(() -> new DeviceBlockingException(
                        "No se ha podido determinar la direccion MAC del router. Vuelve a escanear la red primero."));

        if (device.getIpAddress().equals(gatewayIp) || device.getMacAddress().equalsIgnoreCase(gatewayMac)) {
            throw new DeviceBlockingException("No puedes bloquear el router de tu red: se cortaria toda la conexion.");
        }

        HostAddress victim = new HostAddress(device.getIpAddress(), device.getMacAddress());
        HostAddress gateway = new HostAddress(gatewayIp, gatewayMac);
        blockingManager.startBlocking(device.getId(), victim, gateway, ownMac, ownIp);

        device.setBlocked(true);
        device.setBlockedAt(Instant.now());
        repository.save(device);
        return toDto(device);
    }

    @Transactional
    public NetworkDeviceDto unblockDevice(Long id) {
        NetworkDevice device = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe ningun dispositivo con id " + id));
        if (!device.isBlocked()) {
            return toDto(device);
        }

        blockingManager.stopBlocking(device.getId());
        device.setBlocked(false);
        device.setBlockedAt(null);
        repository.save(device);
        return toDto(device);
    }

    private NetworkDeviceDto toDto(NetworkDevice device) {
        return NetworkDeviceDto.fromEntity(device, currentLastScanAt());
    }

    private Instant currentLastScanAt() {
        return scanStateRepository.findById(ScanState.SINGLETON_ID)
                .map(ScanState::getLastScanAt)
                .orElse(null);
    }
}
