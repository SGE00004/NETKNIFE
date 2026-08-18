package com.netknife.tools.digitalfootprint.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "file_metadata_reports")
public class FileMetadataReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analyzed_at", nullable = false)
    private Instant analyzedAt;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "file_type")
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_status", nullable = false)
    private CheckStatus overallStatus;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FileMetadataFinding> findings = new ArrayList<>();

    protected FileMetadataReport() {
        // requerido por JPA
    }

    public FileMetadataReport(Instant analyzedAt, String originalFilename, String fileType, CheckStatus overallStatus) {
        this.analyzedAt = analyzedAt;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.overallStatus = overallStatus;
    }

    public void addFinding(FileMetadataFinding finding) {
        finding.setReport(this);
        findings.add(finding);
    }

    public Long getId() {
        return id;
    }

    public Instant getAnalyzedAt() {
        return analyzedAt;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getFileType() {
        return fileType;
    }

    public CheckStatus getOverallStatus() {
        return overallStatus;
    }

    public List<FileMetadataFinding> getFindings() {
        return findings;
    }
}
