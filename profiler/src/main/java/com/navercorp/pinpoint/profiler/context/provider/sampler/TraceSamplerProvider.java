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
import com.navercorp.pinpoint.profiler.context.config.ContextConfig;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.sampler.BasicTraceSampler;
import com.navercorp.pinpoint.profiler.sampler.RateLimitTraceSampler;
import com.navercorp.pinpoint.profiler.sampler.SamplerType;
import com.navercorp.pinpoint.profiler.sampler.UrlTraceSampler;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceSamplerProvider implements Provider<TraceSampler> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Sampler sampler;
    private final IdGenerator idGenerator;
    private final ContextConfig contextConfig;
    private final ProfilerConfig profilerConfig;

    @Inject
    public TraceSamplerProvider(ProfilerConfig profilerConfig, ContextConfig contextConfig, Sampler sampler, IdGenerator idGenerator) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.contextConfig = Objects.requireNonNull(contextConfig, "contextConfig");
        this.sampler = Objects.requireNonNull(sampler, "sampler");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }

    @Override
    public TraceSampler get() {
        final TraceSampler traceSampler = newTraceSampler(idGenerator, sampler, contextConfig.getSamplingNewThroughput(), contextConfig.getSamplingContinueThroughput());
        final SamplerConfig samplerConfig = new SamplerConfig(profilerConfig);
        if (Boolean.FALSE == samplerConfig.isSamplingEnable() || Boolean.FALSE == samplerConfig.isUrlSamplingEnable()) {
            logger.info("TraceSamplerProvider {}", traceSampler);
            return traceSampler;
        }

        final SamplerType samplerType = samplerConfig.getSamplerType();
        final TraceSampler urlTraceSampler = newUrlSampler(traceSampler, samplerType);
        logger.info("TraceSamplerProvider {}", urlTraceSampler);
        return urlTraceSampler;
    }

    TraceSampler newTraceSampler(IdGenerator idGenerator, Sampler sampler, int samplingNewThroughput, int samplingContinueThroughput) {
        TraceSampler traceSampler = new BasicTraceSampler(idGenerator, sampler);
        if (samplingNewThroughput > 0 || samplingContinueThroughput > 0) {
            traceSampler = new RateLimitTraceSampler(samplingNewThroughput, samplingContinueThroughput, idGenerator, traceSampler);
        }
        return traceSampler;
    }

    TraceSampler newUrlSampler(TraceSampler defaultTraceSampler, SamplerType samplerType) {
        final Map<String, TraceSampler> urlMap = new LinkedHashMap<>();
        final UrlSamplerConfig urlSamplerConfig = new UrlSamplerConfig(profilerConfig, samplerType);
        for (Map.Entry<Integer, UrlSamplerConfig.UrlInfo> entry : urlSamplerConfig.entryList()) {
            final UrlSamplerConfig.UrlInfo urlInfo = entry.getValue();
            if (urlInfo == null || Boolean.FALSE == urlInfo.isValid()) {
                continue;
            }
            final TraceSampler traceSampler = newTraceSampler(idGenerator, urlInfo.getSampler(), urlInfo.getSamplingNewThroughput(), urlInfo.getSamplingContinueThroughput());
            urlMap.put(urlInfo.getUrlPath(), traceSampler);
        }
        return new UrlTraceSampler(urlMap, defaultTraceSampler);
    }
}
