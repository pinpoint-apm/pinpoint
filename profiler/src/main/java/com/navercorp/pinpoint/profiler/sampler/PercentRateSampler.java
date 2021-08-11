/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author emeroad
 * @author yjqg6666
 */
public class PercentRateSampler implements Sampler {
    // Supported range 100% ~ 0.01%
    public static final long MULTIPLIER = 100;
    public static final long MAX = 100 * MULTIPLIER;

    private final AtomicLong counter = new AtomicLong(0);

    private volatile long samplingRate;

    public PercentRateSampler(long samplingRate) {
        updateSamplingRate(samplingRate);
    }

    public void updateSamplingRate(long samplingRate) {
        if (samplingRate <= 0 || samplingRate >= MAX) {
            // Use TrueSampler for 100%
            throw new IllegalArgumentException("Invalid samplingRate " + samplingRate);
        }
        this.samplingRate = samplingRate;
    }

    @Override
    public boolean isSampling() {
        final long seed = counter.addAndGet(samplingRate);
        final long remainder = MathUtils.floorMod(seed, MAX);
        if (remainder > 0 && remainder <= samplingRate) {
            return true;
        }
        return false;
    }

    @Override
    public int getSamplingRate() {
        return (int) samplingRate;
    }

    @Override
    public void updateSamplingRate(int rate) {
        updateSamplingRate((long) rate);
    }

    @Override
    public String toString() {
        return "PercentRateSampler{" +
                "seedGen=" + counter +
                ", samplingRate=" + samplingRate +
                '}';
    }
}
