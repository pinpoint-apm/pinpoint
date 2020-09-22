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

package com.navercorp.pinpoint.profiler.sampler;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class SamplingRateSampler implements Sampler {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int samplingRate;
    private final int samplingMaxSeqNum;
    private final int samplingOutOfNum;

    public SamplingRateSampler(int samplingRate) {
        if (samplingRate <= 0 || samplingRate > 100) {
            throw new IllegalArgumentException("Invalid samplingRate " + samplingRate);
        }
        this.samplingRate = samplingRate;
        boolean percentSampling = (samplingRate % 10) != 0;
        if (percentSampling) { //ex. 1% 15%
            this.samplingOutOfNum = 100;
            this.samplingMaxSeqNum = samplingRate;
        } else { // 10% 20% 30% ...
            this.samplingOutOfNum = 10;
            this.samplingMaxSeqNum = samplingRate / 10;
        }
    }

    @Override
    public boolean isSampling() {
        int samplingCount = MathUtils.fastAbs(counter.getAndIncrement());
        int mod = samplingCount % samplingOutOfNum;
        return mod < samplingMaxSeqNum;
    }

    @Override
    public String toString() {
        return "SamplingRateSampler{" +
                    "counter=" + counter +
                    "samplingRate=" + samplingRate +
                '}';
    }
}
