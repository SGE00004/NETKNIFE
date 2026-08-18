package com.netknife.tools.cryptojackingdetector.detection;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SuspiciousProcessTrackerTest {

    @Test
    void notSustainedBeforeTheRequiredNumberOfReadings() {
        SuspiciousProcessTracker tracker = new SuspiciousProcessTracker();

        assertThat(tracker.recordReadingAndCheckSustained(100, true, 3)).isFalse();
        assertThat(tracker.recordReadingAndCheckSustained(100, true, 3)).isFalse();
    }

    @Test
    void sustainedOnceTheRequiredNumberOfReadingsIsReached() {
        SuspiciousProcessTracker tracker = new SuspiciousProcessTracker();

        tracker.recordReadingAndCheckSustained(100, true, 3);
        tracker.recordReadingAndCheckSustained(100, true, 3);

        assertThat(tracker.recordReadingAndCheckSustained(100, true, 3)).isTrue();
    }

    @Test
    void aReadingBelowTheThresholdResetsTheStreak() {
        SuspiciousProcessTracker tracker = new SuspiciousProcessTracker();

        tracker.recordReadingAndCheckSustained(100, true, 3);
        tracker.recordReadingAndCheckSustained(100, true, 3);
        tracker.recordReadingAndCheckSustained(100, false, 3);

        assertThat(tracker.recordReadingAndCheckSustained(100, true, 3)).isFalse();
    }

    @Test
    void retainOnlyForgetsPidsThatNoLongerExist() {
        SuspiciousProcessTracker tracker = new SuspiciousProcessTracker();
        tracker.recordReadingAndCheckSustained(100, true, 3);
        tracker.recordReadingAndCheckSustained(100, true, 3);

        tracker.retainOnly(Set.of()); // 100 ya no existe

        assertThat(tracker.recordReadingAndCheckSustained(100, true, 3)).isFalse();
    }
}
