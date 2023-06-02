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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.MathUtils;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class SamplingRateSampler implements Sampler {

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int samplingRate;
    private final boolean sampling;
    private final ProfilerConfig profilerConfig;

    public SamplingRateSampler(boolean sampling, int samplingRate, ProfilerConfig profilerConfig) {
        this.samplingRate = samplingRate;
        this.sampling = sampling;
        this.profilerConfig = profilerConfig;
    }

    @Override
    public boolean isSampling() {
        if(ReSetConfigProcessorFactory.isEnableCollect(profilerConfig)){
            if (!sampling || samplingRate <= 0) {
                return false;
            }
            int samplingCount = MathUtils.fastAbs(counter.getAndIncrement());
            int isSampling = samplingCount % samplingRate;
            return isSampling == 0;
        }else{
            return false;
        }
    }
    public int getSamplingRate(){
        return this.samplingRate;
    }
    public boolean getSampling(){
        return this.sampling;
    }

    @Override
    public String toString() {
        return "SamplingRateSampler{" +
                    "counter=" + counter +
                    "sampling=" + sampling +
                    "samplingRate=" + samplingRate +
                '}';
    }
}
