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
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.sampler.BasicTraceSampler;
import com.navercorp.pinpoint.profiler.sampler.RateLimitTraceSampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceSamplerProvider implements Provider<TraceSampler> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Sampler sampler;
    private final IdGenerator idGenerator;
    private final ProfilerConfig profilerConfig;

    @Inject
    public TraceSamplerProvider(ProfilerConfig profilerConfig, Sampler sampler, IdGenerator idGenerator) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.sampler = Assert.requireNonNull(sampler, "sampler");
        this.idGenerator = Assert.requireNonNull(idGenerator, "idGenerator");
    }

    @Override
    public TraceSampler get() {
        logger.info("new BasicTraceSampler()");
        TraceSampler traceSampler = new BasicTraceSampler(idGenerator, sampler);
        final int samplingNewThroughput = profilerConfig.getSamplingNewThroughput();
        final int samplingContinueThroughput = profilerConfig.getSamplingContinueThroughput();
        if (samplingNewThroughput > 0 || samplingContinueThroughput > 0) {
            traceSampler = new RateLimitTraceSampler(samplingNewThroughput, samplingContinueThroughput, idGenerator, traceSampler);
            logger.info("new RateLimitTraceSampler {}/{}", samplingNewThroughput, samplingContinueThroughput);
        }
        return traceSampler;
    }
}
