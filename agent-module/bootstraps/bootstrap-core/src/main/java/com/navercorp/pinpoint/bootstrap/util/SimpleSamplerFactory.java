/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.util;

import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class SimpleSamplerFactory {
    // functionally identical to profiler's Sampler  
    public static final SimpleSampler FALSE_SAMPLER = new SimpleFalseSampler();
    public static final SimpleSampler TRUE_SAMPLER = new SimpleTrueSampler();

    public static SimpleSampler createSampler(boolean sampling, int samplingRate) {
        if (!sampling || samplingRate <= 0) {
            return FALSE_SAMPLER;
        }
        if (samplingRate == 1) {
            return TRUE_SAMPLER;
        }
        return new SamplingRateSampler(samplingRate);
    }

    public static class SimpleTrueSampler implements SimpleSampler {
        @Override
        public boolean isSampling() {
            return true;
        }
    }

    public static class SimpleFalseSampler implements SimpleSampler {
        @Override
        public boolean isSampling() {
            return false;
        }
    }

    public static class SamplingRateSampler implements SimpleSampler {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final int samplingRate;

        public SamplingRateSampler(int samplingRate) {
            if (samplingRate <= 0) {
                throw new IllegalArgumentException("Invalid samplingRate " + samplingRate);
            }
            this.samplingRate = samplingRate;
        }

        @Override
        public boolean isSampling() {
            int samplingCount = MathUtils.fastAbs(counter.getAndIncrement());
            int isSampling = samplingCount % samplingRate;
            return isSampling == 0;
        }
    }
}
