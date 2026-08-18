package com.netknife.tools.exposurechecker;

import com.netknife.tools.exposurechecker.dto.ExposureReportDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exposure")
public class ExposureCheckerController {

    private final ExposureCheckerService service;

    public ExposureCheckerController(ExposureCheckerService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ExposureReportDto check() {
        return service.check();
    }

    @GetMapping("/last-report")
    public ResponseEntity<ExposureReportDto> lastReport() {
        return service.getLastReport()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
