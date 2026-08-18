package com.netknife.tools.digitalfootprint;

import com.netknife.tools.digitalfootprint.model.FileMetadataReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataReportRepository extends JpaRepository<FileMetadataReport, Long> {

    Optional<FileMetadataReport> findTopByOrderByAnalyzedAtDesc();
}
