package com.netknife.tools.digitalfootprint.check;

import com.netknife.common.dto.CheckStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataRiskClassifierTest {

    @Test
    void gpsLocationIsDanger() {
        assertThat(MetadataRiskClassifier.classify("Ubicacion GPS").status()).isEqualTo(CheckStatus.PELIGRO);
    }

    @Test
    void authorIsAttention() {
        assertThat(MetadataRiskClassifier.classify("Autor").status()).isEqualTo(CheckStatus.ATENCION);
    }

    @Test
    void organizationIsAttention() {
        assertThat(MetadataRiskClassifier.classify("Organizacion").status()).isEqualTo(CheckStatus.ATENCION);
    }

    @Test
    void softwareUsedIsOk() {
        assertThat(MetadataRiskClassifier.classify("Software usado").status()).isEqualTo(CheckStatus.OK);
    }

    @Test
    void datesAreOk() {
        assertThat(MetadataRiskClassifier.classify("Fecha de creacion").status()).isEqualTo(CheckStatus.OK);
        assertThat(MetadataRiskClassifier.classify("Fecha de modificacion").status()).isEqualTo(CheckStatus.OK);
    }
}
