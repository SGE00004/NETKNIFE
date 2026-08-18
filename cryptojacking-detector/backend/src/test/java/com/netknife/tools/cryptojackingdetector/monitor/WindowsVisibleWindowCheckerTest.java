package com.netknife.tools.cryptojackingdetector.monitor;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WindowsVisibleWindowCheckerTest {

    @Test
    void parsesAListOfPidsOnePerLine() {
        String output = "1234\r\n5678\r\n9012\r\n";

        assertThat(WindowsVisibleWindowChecker.parse(output)).containsExactlyInAnyOrder(1234, 5678, 9012);
    }

    @Test
    void ignoresBlankLines() {
        String output = "1234\r\n\r\n5678\r\n";

        assertThat(WindowsVisibleWindowChecker.parse(output)).containsExactlyInAnyOrder(1234, 5678);
    }

    @Test
    void emptyOutputMeansNoVisibleWindows() {
        assertThat(WindowsVisibleWindowChecker.parse("")).isEqualTo(Set.of());
    }
}
