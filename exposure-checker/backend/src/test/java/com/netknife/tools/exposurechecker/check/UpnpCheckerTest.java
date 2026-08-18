package com.netknife.tools.exposurechecker.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpnpCheckerTest {

    @Test
    void upnpRespondingIsAttentionNotDanger() {
        // UPnP activo no es necesariamente peligroso, solo digno de revision.
        CheckResult result = UpnpChecker.classify(true, false);
        assertThat(result.status()).isEqualTo(CheckStatus.ATENCION);
    }

    @Test
    void noResponseIsOk() {
        CheckResult result = UpnpChecker.classify(false, false);
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void ioErrorIsNotVerifiableNeverOk() {
        CheckResult result = UpnpChecker.classify(false, true);
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }
}
