package com.netknife.tools.findingsreport;

import com.netknife.tools.findingsreport.dto.FindingsReportDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/findings-report")
public class FindingsReportController {

    private final FindingsReportService service;

    public FindingsReportController(FindingsReportService service) {
        this.service = service;
    }

    @GetMapping
    public FindingsReportDto report() {
        return service.buildReport();
    }
}
