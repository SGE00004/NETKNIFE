package com.netknife.tools.cryptojackingdetector;

import com.netknife.tools.cryptojackingdetector.alert.CryptojackingAlertService;
import com.netknife.tools.cryptojackingdetector.dto.CryptojackingAlertDto;
import com.netknife.tools.cryptojackingdetector.dto.CryptojackingStatusDto;
import com.netknife.tools.cryptojackingdetector.dto.KillProcessResultDto;
import com.netknife.tools.cryptojackingdetector.process.ProcessTerminationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptojackingDetectorService {

    private final CryptojackingAlertService alertService;
    private final ProcessTerminationService terminationService;

    public CryptojackingDetectorService(CryptojackingAlertService alertService, ProcessTerminationService terminationService) {
        this.alertService = alertService;
        this.terminationService = terminationService;
    }

    public CryptojackingStatusDto status() {
        List<CryptojackingAlertDto> activeAlerts = alertService.activeAlerts();
        String overallStatus = activeAlerts.isEmpty() ? "VERDE" : "ROJO";
        return new CryptojackingStatusDto(overallStatus, activeAlerts);
    }

    public List<CryptojackingAlertDto> history(int limit) {
        return alertService.history(limit);
    }

    public KillProcessResultDto killProcess(long pid) {
        return terminationService.kill(pid);
    }
}
