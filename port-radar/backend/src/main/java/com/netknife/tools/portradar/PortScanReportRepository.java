package com.netknife.tools.portradar;

import com.netknife.tools.portradar.model.PortScanReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortScanReportRepository extends JpaRepository<PortScanReport, Long> {

    Optional<PortScanReport> findTopByOrderByScannedAtDesc();
}
