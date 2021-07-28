/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.sampler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.sampler.FalseSampler;
import com.navercorp.pinpoint.profiler.sampler.CountingSamplerFactory;
import com.navercorp.pinpoint.profiler.sampler.PercentSamplerFactory;
import com.navercorp.pinpoint.profiler.sampler.SamplerFactory;
import com.navercorp.pinpoint.profiler.sampler.SamplerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SamplerProvider implements Provider<Sampler> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;

    @Inject
    public SamplerProvider(ProfilerConfig profilerConfig) {
        this.profilerConfig = profilerConfig;
    }

    @Override
    public Sampler get() {
        SamplerConfig config = new SamplerConfig(profilerConfig);
        logger.info("SamplerConfig:{}", config);

        if (!config.isSamplingEnable()) {
            return FalseSampler.INSTANCE;
        }

        SamplerType samplerType = config.getSamplerType();
        SamplerFactory samplerFactory = newSamplerFactory(samplerType, profilerConfig);

        return samplerFactory.createSampler();
    }

    private SamplerFactory newSamplerFactory(SamplerType samplerType, ProfilerConfig profilerConfig) {
        switch (samplerType) {
            case COUNTING:
                return newCountingSamplerFactory(profilerConfig);
            case PERCENT:
                return newPercentSamplerFactory(profilerConfig);
            default:
                // parse fail
                throw new IllegalStateException("Unexpected samplerType: " + samplerType);
        }
    }

    private SamplerFactory newCountingSamplerFactory(ProfilerConfig profilerConfig) {

        CountingSamplerFactory.Config config = CountingSamplerFactory.config(profilerConfig);
        logger.info("CountingSamplerFactory.Config:{}", config);

        return new CountingSamplerFactory(config);
    }

    private SamplerFactory newPercentSamplerFactory(ProfilerConfig profilerConfig) {

        PercentSamplerFactory.Config config = PercentSamplerFactory.config(profilerConfig);
        logger.info("PercentSamplerFactory.Config:{}", config);

        return new PercentSamplerFactory(config);
    }


}
