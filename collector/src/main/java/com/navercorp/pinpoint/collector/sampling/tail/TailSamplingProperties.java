package com.navercorp.pinpoint.collector.sampling.tail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TailSamplingProperties {

    private boolean enable = false;
    private Duration bufferTtl = Duration.ofSeconds(300);
    private Duration sweepInterval = Duration.ofSeconds(5);
    private Duration decisionTtl = Duration.ofSeconds(600);
    private List<Band> bands = new ArrayList<>();

    /** elapsedMillis 가 속하는 첫 밴드의 수집률(%)을 반환. 매칭 없으면 100(keep, fail-safe). */
    public int rateFor(long elapsedMillis) {
        for (Band band : bands) {
            if (band.getMaxElapsed() == null) {
                return band.getRate(); // catch-all
            }
            if (elapsedMillis < band.getMaxElapsed().toMillis()) {
                return band.getRate();
            }
        }
        return 100;
    }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }
    public Duration getBufferTtl() { return bufferTtl; }
    public void setBufferTtl(Duration bufferTtl) { this.bufferTtl = bufferTtl; }
    public Duration getSweepInterval() { return sweepInterval; }
    public void setSweepInterval(Duration sweepInterval) { this.sweepInterval = sweepInterval; }
    public Duration getDecisionTtl() { return decisionTtl; }
    public void setDecisionTtl(Duration decisionTtl) { this.decisionTtl = decisionTtl; }
    public List<Band> getBands() { return bands; }
    public void setBands(List<Band> bands) { this.bands = bands; }

    public static class Band {
        private Duration maxElapsed; // null => catch-all (must be last)
        private int rate;            // 0..100

        public Duration getMaxElapsed() { return maxElapsed; }
        public void setMaxElapsed(Duration maxElapsed) { this.maxElapsed = maxElapsed; }
        public int getRate() { return rate; }
        public void setRate(int rate) { this.rate = rate; }
    }
}
