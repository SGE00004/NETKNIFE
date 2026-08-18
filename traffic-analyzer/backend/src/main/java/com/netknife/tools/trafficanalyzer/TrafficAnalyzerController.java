package com.netknife.tools.trafficanalyzer;

import com.netknife.tools.trafficanalyzer.dto.ActiveConnectionDto;
import com.netknife.tools.trafficanalyzer.dto.CaptureStatusDto;
import com.netknife.tools.trafficanalyzer.dto.TrafficCaptureCapabilityDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/traffic-analyzer")
public class TrafficAnalyzerController {

    private final TrafficAnalyzerService service;

    public TrafficAnalyzerController(TrafficAnalyzerService service) {
        this.service = service;
    }

    @GetMapping("/capability")
    public TrafficCaptureCapabilityDto capability() {
        return service.getCapability();
    }

    @PostMapping("/capture/start")
    public CaptureStatusDto start() {
        return service.start();
    }

    @PostMapping("/capture/stop")
    public CaptureStatusDto stop() {
        return service.stop();
    }

    @GetMapping("/capture/status")
    public CaptureStatusDto status() {
        return service.status();
    }

    @GetMapping("/connections")
    public List<ActiveConnectionDto> connections() {
        return service.listConnections();
    }
}
