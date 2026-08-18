package com.netknife.tools.hygienechecklist;

import com.netknife.tools.hygienechecklist.model.HygieneManualStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HygieneManualStatusRepository extends JpaRepository<HygieneManualStatus, String> {
}
