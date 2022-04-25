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
 * @author yjqg6666
 */
public class PercentSamplerFactory implements SamplerFactory {

    private final long samplingRate;

    public PercentSamplerFactory(long samplingRate) {
        this.samplingRate = samplingRate;
    }

    public PercentSamplerFactory(Config config) {
        this(config.getSamplingRate());
    }

    @Override
    public Sampler createSampler() {
        if (samplingRate <= 0) {
            return FalseSampler.INSTANCE;
        }
        return new PercentRateSampler(samplingRate);
    }

    public static Config config(ProfilerConfig profilerConfig) {
        String samplingRateStr = profilerConfig.readString("profiler.sampling.percent.sampling-rate", "100");
        long samplingRate = parseSamplingRate(samplingRateStr);
        return new Config(samplingRate);
    }

    static long parseSamplingRate(String samplingRateStr) {
        double samplingRateDouble = Double.parseDouble(samplingRateStr);
        return (long) (samplingRateDouble * PercentRateSampler.MULTIPLIER);
    }

    public static class Config {

        private final long samplingRate;

        public Config(long samplingRate) {
            this.samplingRate = samplingRate;
        }

        public long getSamplingRate() {
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
