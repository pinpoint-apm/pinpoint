/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.sampling.tail;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TailSamplingProperties {

    private boolean enable = false;
    private Duration bufferTtl = Duration.ofSeconds(300);
    private Duration sweepInterval = Duration.ofSeconds(5);
    private Duration decisionTtl = Duration.ofSeconds(600);
    private boolean keepOnError = true;
    // Grace window: when a band would drop a trace, hold the decision this long so a late-arriving
    // errored span (e.g. a downstream tier) can flip it to keep before it is finalized.
    // Must stay below bufferTtl. Longer grace catches later errors but buffers more in Redis.
    private Duration decisionGrace = Duration.ofSeconds(60);
    private List<Band> bands = new ArrayList<>();

    /** Returns the sampling rate (%) of the first band matching elapsedMillis. Returns 100 (keep, fail-safe) when no band matches. */
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
    public boolean isKeepOnError() { return keepOnError; }
    public void setKeepOnError(boolean keepOnError) { this.keepOnError = keepOnError; }
    public Duration getDecisionGrace() { return decisionGrace; }
    public void setDecisionGrace(Duration decisionGrace) { this.decisionGrace = decisionGrace; }
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
