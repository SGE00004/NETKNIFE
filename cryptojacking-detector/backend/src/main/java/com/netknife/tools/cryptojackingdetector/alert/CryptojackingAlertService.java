package com.netknife.tools.cryptojackingdetector.alert;

import com.netknife.tools.cryptojackingdetector.alert.model.CryptojackingAlert;
import com.netknife.tools.cryptojackingdetector.detection.SuspicionResult;
import com.netknife.tools.cryptojackingdetector.dto.CryptojackingAlertDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reconcilia las sospechas detectadas en cada poll con el historial persistido:
 * crea una alerta nueva la primera vez que se ve un PID sospechoso, actualiza el
 * pico de CPU mientras sigue activa, y la resuelve automaticamente si deja de ser
 * sospechosa (distinguiendo si el proceso sigue vivo con CPU normal, o si termino
 * por su cuenta).
 */
@Service
public class CryptojackingAlertService {

    private final CryptojackingAlertRepository repository;

    public CryptojackingAlertService(CryptojackingAlertRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void reconcile(List<SuspicionResult> currentSuspicions, Set<Long> allLivePids) {
        Instant now = Instant.now();
        Set<Long> suspiciousPids = currentSuspicions.stream().map(SuspicionResult::pid).collect(Collectors.toSet());

        for (SuspicionResult suspicion : currentSuspicions) {
            CryptojackingAlert alert = repository.findByPidAndResolvedAtIsNull(suspicion.pid())
                    .orElseGet(() -> repository.save(new CryptojackingAlert(
                            suspicion.pid(), suspicion.processName(), suspicion.processPath(),
                            suspicion.reason(), suspicion.cpuPercent(), now)));
            alert.updatePeakCpu(suspicion.cpuPercent());
        }

        for (CryptojackingAlert active : repository.findByResolvedAtIsNull()) {
            if (suspiciousPids.contains(active.getPid())) {
                continue;
            }
            AlertResolution resolution = allLivePids.contains(active.getPid())
                    ? AlertResolution.CPU_DROPPED
                    : AlertResolution.PROCESS_EXITED_ON_ITS_OWN;
            active.resolve(resolution, now);
        }
    }

    @Transactional(readOnly = true)
    public List<CryptojackingAlertDto> activeAlerts() {
        return repository.findByResolvedAtIsNull().stream().map(CryptojackingAlertDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<CryptojackingAlertDto> history(int limit) {
        return repository.findAllByOrderByDetectedAtDesc(PageRequest.of(0, limit)).stream()
                .map(CryptojackingAlertDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasActiveAlertForPid(long pid) {
        return repository.findByPidAndResolvedAtIsNull(pid).isPresent();
    }

    @Transactional
    public void markResolvedByUserKill(long pid) {
        repository.findByPidAndResolvedAtIsNull(pid)
                .ifPresent(alert -> alert.resolve(AlertResolution.PROCESS_ENDED_BY_USER, Instant.now()));
    }
}
