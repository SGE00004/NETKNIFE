package com.netknife.tools.networkscanner;

import com.netknife.tools.networkscanner.model.ScanState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanStateRepository extends JpaRepository<ScanState, Long> {
}
