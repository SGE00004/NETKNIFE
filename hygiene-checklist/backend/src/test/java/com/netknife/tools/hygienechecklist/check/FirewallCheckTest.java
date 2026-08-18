package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FirewallCheckTest {

    @Test
    void allProfilesOnIsOk() {
        CheckResult result = FirewallCheck.classifyWindows(List.of("ON", "ON", "ON"));
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void spanishActivadoCountsAsOn() {
        CheckResult result = FirewallCheck.classifyWindows(List.of("Activado", "Activado", "Activado"));
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void oneProfileOffIsDanger() {
        CheckResult result = FirewallCheck.classifyWindows(List.of("ON", "ON", "OFF"));
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void noProfilesParsedIsNotVerifiable() {
        CheckResult result = FirewallCheck.classifyWindows(List.of());
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    @Test
    void macEnabledIsOk() {
        CheckResult result = FirewallCheck.classifyMac("Firewall is enabled. (State = 1)");
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void macDisabledIsDanger() {
        CheckResult result = FirewallCheck.classifyMac("Firewall is disabled. (State = 0)");
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void macUnrecognizedOutputIsNotVerifiable() {
        CheckResult result = FirewallCheck.classifyMac("something unexpected");
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }
}
