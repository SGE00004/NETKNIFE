package com.netknife.common.wifi;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WifiEncryptionCheckerTest {

    @Test
    void wpa3IsOk() {
        CheckResult result = WifiEncryptionChecker.classify(true, "WPA3-Personal");
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void wpa2IsOk() {
        CheckResult result = WifiEncryptionChecker.classify(true, "WPA2-Personal");
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void plainWpaWithoutTheNumberIsAttention() {
        // "WPA-Personal" contiene "WPA" pero no "WPA2" ni "WPA3": debe distinguirse
        // de WPA2/WPA3, es el caso mas facil de confundir por substring.
        CheckResult result = WifiEncryptionChecker.classify(true, "WPA-Personal");
        assertThat(result.status()).isEqualTo(CheckStatus.ATENCION);
    }

    @Test
    void wepIsDanger() {
        CheckResult result = WifiEncryptionChecker.classify(true, "WEP");
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void openNetworkIsDanger() {
        CheckResult result = WifiEncryptionChecker.classify(true, "Open");
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void noneAuthValueIsDanger() {
        CheckResult result = WifiEncryptionChecker.classify(true, "None");
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void notConnectedIsNotVerifiableNeverGreen() {
        CheckResult result = WifiEncryptionChecker.classify(false, "WPA2-Personal");
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    @Test
    void missingAuthValueIsNotVerifiable() {
        CheckResult result = WifiEncryptionChecker.classify(true, null);
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    @Test
    void blankAuthValueIsNotVerifiable() {
        CheckResult result = WifiEncryptionChecker.classify(true, "   ");
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    @Test
    void unrecognizedAuthValueIsNotVerifiableRatherThanGuessed() {
        CheckResult result = WifiEncryptionChecker.classify(true, "802.1X-EAP-SOMETHING-WEIRD");
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }
}
