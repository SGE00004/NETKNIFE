package com.netknife.tools.exposurechecker;

import com.netknife.tools.exposurechecker.model.ExposureReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExposureReportRepository extends JpaRepository<ExposureReport, Long> {

    Optional<ExposureReport> findTopByOrderByCheckedAtDesc();
}
