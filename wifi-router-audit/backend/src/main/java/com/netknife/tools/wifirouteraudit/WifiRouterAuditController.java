package com.netknife.tools.wifirouteraudit;

import com.netknife.tools.wifirouteraudit.dto.RunAuditRequest;
import com.netknife.tools.wifirouteraudit.dto.WifiRouterAuditReportDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wifi-router-audit")
public class WifiRouterAuditController {

    private final WifiRouterAuditService service;

    public WifiRouterAuditController(WifiRouterAuditService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public WifiRouterAuditReportDto check(@RequestBody(required = false) RunAuditRequest request) {
        String routerAddress = request == null ? null : request.routerAddress();
        return service.runAudit(routerAddress);
    }

    @GetMapping("/last-report")
    public ResponseEntity<WifiRouterAuditReportDto> lastReport() {
        return service.getLastReport()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
