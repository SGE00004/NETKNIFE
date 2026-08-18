package com.netknife.tools.cryptojackingdetector.process;

import com.netknife.common.exception.ActionRejectedException;
import com.netknife.tools.cryptojackingdetector.alert.CryptojackingAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessTerminationServiceTest {

    @Mock
    private CryptojackingAlertService alertService;

    private ProcessTerminationService service;

    @BeforeEach
    void setUp() {
        service = new ProcessTerminationService(alertService);
    }

    @Test
    void rejectsProtectedSystemPids() {
        assertThatThrownBy(() -> service.kill(0)).isInstanceOf(ActionRejectedException.class);
        assertThatThrownBy(() -> service.kill(4)).isInstanceOf(ActionRejectedException.class);
        verifyNoInteractions(alertService);
    }

    @Test
    void rejectsKillingItsOwnProcess() {
        long ownPid = ProcessHandle.current().pid();

        assertThatThrownBy(() -> service.kill(ownPid)).isInstanceOf(ActionRejectedException.class);
        verifyNoInteractions(alertService);
    }

    @Test
    void rejectsAPidWithoutAnActiveAlert() {
        when(alertService.hasActiveAlertForPid(999_999L)).thenReturn(false);

        assertThatThrownBy(() -> service.kill(999_999L))
                .isInstanceOf(ActionRejectedException.class)
                .hasMessageContaining("sospechosos activamente");
    }
}
