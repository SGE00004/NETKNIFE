package com.netknife.tools.trafficanalyzer;

import com.netknife.common.geo.GeoLocation;
import com.netknife.common.geo.IpGeolocationLookup;
import com.netknife.tools.trafficanalyzer.capture.ActiveConnection;
import com.netknife.tools.trafficanalyzer.capture.ConnectionKey;
import com.netknife.tools.trafficanalyzer.capture.TrafficCaptureCapabilityService;
import com.netknife.tools.trafficanalyzer.capture.TrafficCaptureManager;
import com.netknife.tools.trafficanalyzer.dto.ActiveConnectionDto;
import com.netknife.tools.trafficanalyzer.dto.CaptureStatusDto;
import com.netknife.tools.trafficanalyzer.dto.TrafficCaptureCapabilityDto;
import com.netknife.tools.trafficanalyzer.process.NetstatConnection;
import com.netknife.tools.trafficanalyzer.process.NetstatProcessConnectionMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TrafficAnalyzerService {

    private final TrafficCaptureCapabilityService capabilityService;
    private final TrafficCaptureManager captureManager;
    private final NetstatProcessConnectionMapper processMapper;
    private final IpGeolocationLookup geoLookup;
    private final long newConnectionWindowSeconds;

    public TrafficAnalyzerService(
            TrafficCaptureCapabilityService capabilityService,
            TrafficCaptureManager captureManager,
            NetstatProcessConnectionMapper processMapper,
            IpGeolocationLookup geoLookup,
            @Value("${netknife.traffic-analyzer.new-connection-window-seconds:10}") long newConnectionWindowSeconds) {
        this.capabilityService = capabilityService;
        this.captureManager = captureManager;
        this.processMapper = processMapper;
        this.geoLookup = geoLookup;
        this.newConnectionWindowSeconds = newConnectionWindowSeconds;
    }

    public TrafficCaptureCapabilityDto getCapability() {
        TrafficCaptureCapabilityService.Capability capability = capabilityService.current();
        return new TrafficCaptureCapabilityDto(capability.available(), capability.reason().name(), capability.message());
    }

    public CaptureStatusDto start() {
        TrafficCaptureCapabilityService.Capability capability = capabilityService.refresh();
        if (capability.available()) {
            captureManager.start();
        }
        return status();
    }

    public CaptureStatusDto stop() {
        captureManager.stop();
        return status();
    }

    public CaptureStatusDto status() {
        TrafficCaptureCapabilityService.Capability capability = capabilityService.current();
        return new CaptureStatusDto(captureManager.isRunning(), capability.available(),
                capability.reason().name(), capability.message());
    }

    public List<ActiveConnectionDto> listConnections() {
        if (!captureManager.isRunning()) {
            return List.of();
        }
        List<NetstatConnection> netstatSnapshot = processMapper.currentSnapshot();
        Instant now = Instant.now();
        return captureManager.snapshot().stream()
                .map(connection -> toDto(connection, netstatSnapshot, now))
                .toList();
    }

    private ActiveConnectionDto toDto(ActiveConnection connection, List<NetstatConnection> netstatSnapshot, Instant now) {
        ConnectionKey key = connection.getKey();
        Optional<NetstatConnection> owner = processMapper.findOwner(
                key.protocol(), key.localPort(), key.remoteIp(), key.remotePort(), netstatSnapshot);
        Optional<GeoLocation> geo = geoLookup.lookup(key.remoteIp());
        boolean isNew = Duration.between(connection.getFirstSeen(), now).getSeconds() <= newConnectionWindowSeconds;

        return new ActiveConnectionDto(
                key.remoteIp(),
                key.remotePort(),
                key.protocol(),
                connection.isEncrypted(),
                owner.map(NetstatConnection::processName).orElse(null),
                owner.map(NetstatConnection::pid).orElse(null),
                geo.map(GeoLocation::country).orElse(null),
                geo.map(GeoLocation::city).orElse(null),
                geo.map(GeoLocation::isp).orElse(null),
                geo.map(GeoLocation::org).orElse(null),
                connection.getFirstSeen(),
                connection.getLastSeen(),
                isNew);
    }
}
