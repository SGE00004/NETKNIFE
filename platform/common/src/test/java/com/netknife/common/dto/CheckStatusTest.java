package com.netknife.common.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckStatusTest {

    @Test
    void worstOfPrefersDangerOverEverythingElse() {
        assertThat(CheckStatus.worstOf(CheckStatus.OK, CheckStatus.ATENCION, CheckStatus.PELIGRO))
                .isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void worstOfPrefersAttentionOverNotVerifiableAndOk() {
        assertThat(CheckStatus.worstOf(CheckStatus.OK, CheckStatus.NO_VERIFICABLE, CheckStatus.ATENCION))
                .isEqualTo(CheckStatus.ATENCION);
    }

    @Test
    void worstOfNeverTreatsNotVerifiableAsOk() {
        // Que no sepamos si algo esta bien nunca debe verse como "todo en orden".
        assertThat(CheckStatus.worstOf(CheckStatus.OK, CheckStatus.OK, CheckStatus.NO_VERIFICABLE))
                .isEqualTo(CheckStatus.NO_VERIFICABLE);
    }

    @Test
    void worstOfIsOkOnlyWhenEverythingIsOk() {
        assertThat(CheckStatus.worstOf(CheckStatus.OK, CheckStatus.OK, CheckStatus.OK))
                .isEqualTo(CheckStatus.OK);
    }

    @Test
    void worstOfOfEmptyCollectionIsOk() {
        assertThat(CheckStatus.worstOf(java.util.List.of())).isEqualTo(CheckStatus.OK);
    }
}
