package com.netknife.tools.digitalfootprint;

import com.netknife.tools.digitalfootprint.dto.AnalyzeDomainRequest;
import com.netknife.tools.digitalfootprint.dto.DomainFootprintReportDto;
import com.netknife.tools.digitalfootprint.dto.FileMetadataReportDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/digital-footprint")
public class DigitalFootprintController {

    private final FileMetadataService fileMetadataService;
    private final DomainFootprintService domainFootprintService;

    public DigitalFootprintController(FileMetadataService fileMetadataService, DomainFootprintService domainFootprintService) {
        this.fileMetadataService = fileMetadataService;
        this.domainFootprintService = domainFootprintService;
    }

    @PostMapping("/analyze-file")
    public FileMetadataReportDto analyzeFile(@RequestParam("file") MultipartFile file) {
        return fileMetadataService.analyze(file);
    }

    @GetMapping("/last-file-report")
    public ResponseEntity<FileMetadataReportDto> lastFileReport() {
        return fileMetadataService.getLastReport()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/analyze-domain")
    public DomainFootprintReportDto analyzeDomain(@RequestBody AnalyzeDomainRequest request) {
        return domainFootprintService.analyze(request.domain());
    }

    @GetMapping("/last-domain-report")
    public ResponseEntity<DomainFootprintReportDto> lastDomainReport() {
        return domainFootprintService.getLastReport()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
