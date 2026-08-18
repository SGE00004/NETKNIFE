package com.netknife.tools.phishingsimulator;

import com.netknife.tools.phishingsimulator.model.PhishingSimulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhishingSimulationRepository extends JpaRepository<PhishingSimulation, Long> {

    List<PhishingSimulation> findAllByOrderBySentAtDesc();
}
