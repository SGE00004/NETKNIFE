package com.netknife.tools.digitalfootprint.model;

import com.netknife.common.dto.CheckStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "file_metadata_findings")
public class FileMetadataFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private FileMetadataReport report;

    @Column(nullable = false)
    private String category;

    @Column(length = 500)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status;

    @Column(length = 500)
    private String explanation;

    protected FileMetadataFinding() {
        // requerido por JPA
    }

    public FileMetadataFinding(String category, String value, CheckStatus status, String explanation) {
        this.category = category;
        this.value = value;
        this.status = status;
        this.explanation = explanation;
    }

    void setReport(FileMetadataReport report) {
        this.report = report;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getValue() {
        return value;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public String getExplanation() {
        return explanation;
    }
}
