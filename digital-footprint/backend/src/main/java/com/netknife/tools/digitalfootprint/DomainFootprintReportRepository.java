package com.netknife.tools.digitalfootprint;

import com.netknife.tools.digitalfootprint.model.DomainFootprintReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DomainFootprintReportRepository extends JpaRepository<DomainFootprintReport, Long> {

    Optional<DomainFootprintReport> findTopByOrderByAnalyzedAtDesc();
}
