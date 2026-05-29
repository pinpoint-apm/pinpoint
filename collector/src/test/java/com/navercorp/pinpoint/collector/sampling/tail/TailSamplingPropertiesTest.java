package com.navercorp.pinpoint.collector.sampling.tail;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class TailSamplingPropertiesTest {

    private TailSamplingProperties props() {
        TailSamplingProperties p = new TailSamplingProperties();
        TailSamplingProperties.Band b1 = new TailSamplingProperties.Band();
        b1.setMaxElapsed(Duration.ofMillis(50));
        b1.setRate(1);
        TailSamplingProperties.Band b2 = new TailSamplingProperties.Band();
        b2.setMaxElapsed(Duration.ofMillis(100));
        b2.setRate(5);
        TailSamplingProperties.Band b3 = new TailSamplingProperties.Band();
        b3.setMaxElapsed(Duration.ofMillis(500));
        b3.setRate(10);
        TailSamplingProperties.Band b4 = new TailSamplingProperties.Band(); // catch-all (maxElapsed = null)
        b4.setRate(100);
        p.setBands(List.of(b1, b2, b3, b4));
        return p;
    }

    @Test
    void rateFor_matchesFirstBandByUpperBound() {
        TailSamplingProperties p = props();
        assertThat(p.rateFor(49)).isEqualTo(1);
        assertThat(p.rateFor(50)).isEqualTo(5);   // 50 < 50 false -> next band
        assertThat(p.rateFor(99)).isEqualTo(5);
        assertThat(p.rateFor(100)).isEqualTo(10);
        assertThat(p.rateFor(499)).isEqualTo(10);
        assertThat(p.rateFor(500)).isEqualTo(100);
        assertThat(p.rateFor(5000)).isEqualTo(100);
    }

    @Test
    void rateFor_noBandMatches_defaultsToKeep100() {
        TailSamplingProperties p = new TailSamplingProperties();
        p.setBands(List.of()); // empty
        assertThat(p.rateFor(10)).isEqualTo(100); // fail-safe: keep
    }
}
