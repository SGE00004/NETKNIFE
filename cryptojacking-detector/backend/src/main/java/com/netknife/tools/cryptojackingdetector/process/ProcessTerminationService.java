package com.netknife.tools.cryptojackingdetector.process;

import com.netknife.common.exception.ActionRejectedException;
import com.netknife.tools.cryptojackingdetector.alert.CryptojackingAlertService;
import com.netknife.tools.cryptojackingdetector.dto.KillProcessResultDto;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Finaliza un proceso, con guardarraíles: solo se puede matar un PID que el propio
 * detector haya marcado como sospechoso activo (nunca un PID arbitrario que mande
 * el cliente), nunca el propio proceso de NETKNIFE, y nunca un PID critico del
 * sistema (0 = System Idle Process, 4 = System, en Windows).
 */
@Component
public class ProcessTerminationService {

    private static final Set<Long> PROTECTED_SYSTEM_PIDS = Set.of(0L, 4L);

    private final CryptojackingAlertService alertService;

    public ProcessTerminationService(CryptojackingAlertService alertService) {
        this.alertService = alertService;
    }

    public KillProcessResultDto kill(long pid) {
        if (PROTECTED_SYSTEM_PIDS.contains(pid)) {
            throw new ActionRejectedException("No se puede finalizar un proceso critico del sistema.");
        }
        if (pid == ProcessHandle.current().pid()) {
            throw new ActionRejectedException("No puedes finalizar el propio proceso de NETKNIFE.");
        }
        if (!alertService.hasActiveAlertForPid(pid)) {
            throw new ActionRejectedException(
                    "Solo se pueden finalizar procesos que el detector ha marcado como sospechosos activamente.");
        }

        Optional<ProcessHandle> handle = ProcessHandle.of(pid);
        if (handle.isEmpty()) {
            alertService.markResolvedByUserKill(pid);
            return new KillProcessResultDto(pid, true, "El proceso ya no estaba en ejecucion.");
        }

        boolean requested = handle.get().destroy();
        if (!requested) {
            throw new ActionRejectedException("No se pudo solicitar la finalizacion del proceso.");
        }
        alertService.markResolvedByUserKill(pid);
        return new KillProcessResultDto(pid, true, "Se ha solicitado finalizar el proceso.");
    }
}
