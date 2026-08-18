package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiskEncryptionCheckTest {

    @Test
    void fileVaultOnIsOk() {
        CheckResult result = DiskEncryptionCheck.classifyMac("FileVault is On.");
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void fileVaultOffIsDanger() {
        CheckResult result = DiskEncryptionCheck.classifyMac("FileVault is Off.");
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void macUnrecognizedOutputIsNotVerifiable() {
        CheckResult result = DiskEncryptionCheck.classifyMac("unexpected output");
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    @Test
    void bitLockerProtectionOnIsOk() {
        CheckResult result = DiskEncryptionCheck.classifyWindows("Protection On");
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void bitLockerProtectionOffIsDanger() {
        CheckResult result = DiskEncryptionCheck.classifyWindows("Protection Off");
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void windowsUnrecognizedOutputIsNotVerifiable() {
        CheckResult result = DiskEncryptionCheck.classifyWindows("unexpected output");
        assertThat(result.status()).isEqualTo(CheckStatus.NO_VERIFICABLE);
    }
}
