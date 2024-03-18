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


/**
 * @author emeroad
 */
public class CountingSamplerFactory implements SamplerFactory {

    public static final String LEGACY_SAMPLING_RATE_NAME = "profiler.sampling.rate";
    public static final String SAMPLING_RATE_NAME = "profiler.sampling.counting.sampling-rate";

    private final int samplingRate;

    public CountingSamplerFactory(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public CountingSamplerFactory(Config config) {
        this(config.getSamplingRate());
    }

    @Override
    public Sampler createSampler() {
        if (samplingRate <= 0) {
            return FalseSampler.INSTANCE;
        }
        if (samplingRate == 1) {
            return TrueSampler.INSTANCE;
        }
        return new CountingSampler(samplingRate);
    }

    @Override
    public String toString() {
        return "CountingSamplerFactory{" +
                "samplingRate=" + samplingRate +
                '}';
    }

    public static Config config(ProfilerConfig profilerConfig) {
        int samplingRate = getSamplingRate(profilerConfig);
        return new Config(samplingRate);
    }

    private static int getSamplingRate(ProfilerConfig profilerConfig) {
        int legacy = profilerConfig.readInt(LEGACY_SAMPLING_RATE_NAME, -1);
        if (legacy != -1) {
            return legacy;
        }
        return profilerConfig.readInt(SAMPLING_RATE_NAME, 1);
    }

    public static class Config {
        private final int samplingRate;

        public Config(int samplingRate) {
            this.samplingRate = samplingRate;
        }

        public int getSamplingRate() {
            return samplingRate;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "samplingRate=" + samplingRate +
                    '}';
        }
    }

}
