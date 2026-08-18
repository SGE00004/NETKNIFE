package com.netknife.tools.cryptojackingdetector.alert;

import com.netknife.tools.cryptojackingdetector.alert.model.CryptojackingAlert;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CryptojackingAlertRepository extends JpaRepository<CryptojackingAlert, Long> {

    List<CryptojackingAlert> findByResolvedAtIsNull();

    Optional<CryptojackingAlert> findByPidAndResolvedAtIsNull(long pid);

    List<CryptojackingAlert> findAllByOrderByDetectedAtDesc(Pageable pageable);
}
