package com.netknife.tools.hygienechecklist.check;

import com.netknife.common.dto.CheckResult;
import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AntivirusCheckTest {

    @Test
    void noProductsIsDanger() {
        CheckResult result = AntivirusCheck.classify(List.of());
        assertThat(result.status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void anyRegisteredProductIsOk() {
        CheckResult result = AntivirusCheck.classify(List.of("Windows Defender"));
        assertThat(result.status()).isEqualTo(CheckStatus.OK);
        assertThat(result.summary()).contains("Windows Defender");
    }

    @Test
    void multipleProductsAreAllMentioned() {
        CheckResult result = AntivirusCheck.classify(List.of("Windows Defender", "Malwarebytes"));
        assertThat(result.summary()).contains("Windows Defender").contains("Malwarebytes");
    }
}
