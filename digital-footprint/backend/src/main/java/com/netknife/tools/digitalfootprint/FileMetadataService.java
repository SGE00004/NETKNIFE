package com.netknife.tools.digitalfootprint;

import com.netknife.common.dto.CheckStatus;
import com.netknife.tools.digitalfootprint.check.FileMetadataExtractor;
import com.netknife.tools.digitalfootprint.check.MetadataRiskClassifier;
import com.netknife.tools.digitalfootprint.check.RawMetadataEntry;
import com.netknife.tools.digitalfootprint.dto.FileMetadataReportDto;
import com.netknife.tools.digitalfootprint.model.FileMetadataFinding;
import com.netknife.tools.digitalfootprint.model.FileMetadataReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FileMetadataService {

    private final FileMetadataExtractor extractor;
    private final FileMetadataReportRepository repository;

    public FileMetadataService(FileMetadataExtractor extractor, FileMetadataReportRepository repository) {
        this.extractor = extractor;
        this.repository = repository;
    }

    @Transactional
    public FileMetadataReportDto analyze(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Selecciona un archivo para analizar.");
        }

        List<RawMetadataEntry> rawEntries;
        try {
            rawEntries = extractor.extract(file.getInputStream(), file.getOriginalFilename());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "No se ha podido leer el archivo. Comprueba que es un PDF, Word o imagen valido.");
        }

        List<MetadataRiskClassifier.Classification> classifications = rawEntries.stream()
                .map(entry -> MetadataRiskClassifier.classify(entry.category()))
                .toList();
        CheckStatus overall = CheckStatus.worstOf(classifications.stream()
                .map(MetadataRiskClassifier.Classification::status)
                .toList());

        FileMetadataReport report = new FileMetadataReport(
                Instant.now(), file.getOriginalFilename(), file.getContentType(), overall);
        for (int i = 0; i < rawEntries.size(); i++) {
            RawMetadataEntry entry = rawEntries.get(i);
            MetadataRiskClassifier.Classification classification = classifications.get(i);
            report.addFinding(new FileMetadataFinding(
                    entry.category(), entry.value(), classification.status(), classification.explanation()));
        }

        repository.save(report);
        return FileMetadataReportDto.fromEntity(report);
    }

    @Transactional(readOnly = true)
    public Optional<FileMetadataReportDto> getLastReport() {
        return repository.findTopByOrderByAnalyzedAtDesc().map(FileMetadataReportDto::fromEntity);
    }
}
