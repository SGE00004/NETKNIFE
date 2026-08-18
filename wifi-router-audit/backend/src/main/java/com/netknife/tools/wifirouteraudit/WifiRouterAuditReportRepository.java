package com.netknife.tools.wifirouteraudit;

import com.netknife.tools.wifirouteraudit.model.WifiRouterAuditReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WifiRouterAuditReportRepository extends JpaRepository<WifiRouterAuditReport, Long> {

    Optional<WifiRouterAuditReport> findTopByOrderByCheckedAtDesc();
}
