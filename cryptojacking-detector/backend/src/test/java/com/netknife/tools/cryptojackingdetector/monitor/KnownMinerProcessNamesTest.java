package com.netknife.tools.cryptojackingdetector.monitor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnownMinerProcessNamesTest {

    @Test
    void recognizesAKnownMinerByExactName() {
        assertThat(KnownMinerProcessNames.isKnownMinerName("xmrig")).isTrue();
    }

    @Test
    void recognizesAKnownMinerCaseInsensitiveAndWithExeSuffix() {
        assertThat(KnownMinerProcessNames.isKnownMinerName("XMRig.exe")).isTrue();
    }

    @Test
    void aRegularProcessIsNotAKnownMiner() {
        assertThat(KnownMinerProcessNames.isKnownMinerName("chrome.exe")).isFalse();
    }

    @Test
    void nullNameIsNotAKnownMiner() {
        assertThat(KnownMinerProcessNames.isKnownMinerName(null)).isFalse();
    }
}
