package com.netknife.tools.cryptojackingdetector.alert;

import com.netknife.tools.cryptojackingdetector.alert.model.CryptojackingAlert;
import com.netknife.tools.cryptojackingdetector.detection.SuspicionReason;
import com.netknife.tools.cryptojackingdetector.detection.SuspicionResult;
import com.netknife.tools.cryptojackingdetector.dto.CryptojackingAlertDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptojackingAlertServiceTest {

    @Mock
    private CryptojackingAlertRepository repository;

    private CryptojackingAlertService service;

    @BeforeEach
    void setUp() {
        service = new CryptojackingAlertService(repository);
        lenient().when(repository.save(any(CryptojackingAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsANewAlertTheFirstTimeAPidIsSuspicious() {
        when(repository.findByPidAndResolvedAtIsNull(100L)).thenReturn(Optional.empty());
        when(repository.findByResolvedAtIsNull()).thenReturn(List.of());
        SuspicionResult suspicion = new SuspicionResult(100L, "xmrig.exe", "C:\\x\\xmrig.exe", SuspicionReason.KNOWN_MINER_NAME, 80.0);

        service.reconcile(List.of(suspicion), Set.of(100L));

        verify(repository).save(argThat(alert -> alert.getPid() == 100L
                && alert.getProcessName().equals("xmrig.exe")
                && alert.getReason() == SuspicionReason.KNOWN_MINER_NAME));
    }

    @Test
    void resolvesAsCpuDroppedWhenTheProcessIsStillAliveButNoLongerSuspicious() {
        CryptojackingAlert active = new CryptojackingAlert(200L, "misterioso.exe", null,
                SuspicionReason.SUSTAINED_HIGH_CPU_NO_WINDOW, 90.0, Instant.now());
        when(repository.findByResolvedAtIsNull()).thenReturn(List.of(active));

        service.reconcile(List.of(), Set.of(200L)); // 200 sigue vivo pero ya no aparece como sospechoso

        assertThat(active.getResolution()).isEqualTo(AlertResolution.CPU_DROPPED);
    }

    @Test
    void resolvesAsProcessExitedWhenThePidNoLongerExists() {
        CryptojackingAlert active = new CryptojackingAlert(300L, "misterioso.exe", null,
                SuspicionReason.SUSTAINED_HIGH_CPU_NO_WINDOW, 90.0, Instant.now());
        when(repository.findByResolvedAtIsNull()).thenReturn(List.of(active));

        service.reconcile(List.of(), Set.of()); // 300 ya no esta entre los PIDs vivos

        assertThat(active.getResolution()).isEqualTo(AlertResolution.PROCESS_EXITED_ON_ITS_OWN);
    }

    @Test
    void markResolvedByUserKillSetsTheRightResolution() {
        CryptojackingAlert active = new CryptojackingAlert(400L, "xmrig.exe", null,
                SuspicionReason.KNOWN_MINER_NAME, 95.0, Instant.now());
        when(repository.findByPidAndResolvedAtIsNull(400L)).thenReturn(Optional.of(active));

        service.markResolvedByUserKill(400L);

        assertThat(active.getResolution()).isEqualTo(AlertResolution.PROCESS_ENDED_BY_USER);
        assertThat(active.getResolvedAt()).isNotNull();
    }

    @Test
    void historyMapsEntitiesToDtos() {
        CryptojackingAlert alert = new CryptojackingAlert(500L, "xmrig.exe", null,
                SuspicionReason.KNOWN_MINER_NAME, 95.0, Instant.now());
        when(repository.findAllByOrderByDetectedAtDesc(any())).thenReturn(List.of(alert));

        List<CryptojackingAlertDto> history = service.history(50);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).pid()).isEqualTo(500L);
    }
}
