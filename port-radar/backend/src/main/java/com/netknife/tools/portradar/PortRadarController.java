package com.netknife.tools.portradar;

import com.netknife.tools.portradar.dto.PortScanReportDto;
import com.netknife.tools.portradar.dto.ScanPortsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/port-radar")
public class PortRadarController {

    private final PortRadarService service;

    public PortRadarController(PortRadarService service) {
        this.service = service;
    }

    @PostMapping("/scan")
    public PortScanReportDto scan(@RequestBody ScanPortsRequest request) {
        return service.scan(request.target());
    }

    @GetMapping("/last-report")
    public ResponseEntity<PortScanReportDto> lastReport() {
        return service.getLastReport()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
