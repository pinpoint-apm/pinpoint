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

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "collector.sampling.tail", name = "enable", havingValue = "true")
@EnableConfigurationProperties
@EnableScheduling
@Import(TailSamplingRedisConfig.class)
public class TailSamplingConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "collector.sampling.tail")
    public TailSamplingProperties tailSamplingProperties() {
        return new TailSamplingProperties();
    }

    @Bean
    public BufferedSpanCodec bufferedSpanCodec() {
        return new BufferedSpanCodec();
    }

    @Bean
    public TailSamplingRepository tailSamplingRepository(
            @Qualifier("tailSamplingRedisTemplate") RedisTemplate<String, byte[]> redisTemplate,
            @Qualifier("tailAcceptScript") byte[] acceptScript,
            @Qualifier("tailDecideScript") byte[] decideScript,
            TailSamplingProperties properties) {
        return new TailSamplingRepository(redisTemplate, acceptScript, decideScript,
                properties.getBufferTtl().toSeconds(), properties.getDecisionTtl().toSeconds());
    }

    @Bean
    public TailSampler tailSampler(TraceService[] traceServices,
                                   TailSamplingRepository repository,
                                   TailSamplingProperties properties,
                                   BufferedSpanCodec codec,
                                   GrpcSpanFactory spanFactory,
                                   MeterRegistry meterRegistry) {
        return new TailSampler(traceServices, repository, properties, codec, spanFactory, meterRegistry);
    }

    @Bean
    public TailSamplingSweeper tailSamplingSweeper(TailSamplingRepository repository,
                                                   TailSampler tailSampler,
                                                   TailSamplingProperties properties) {
        return new TailSamplingSweeper(repository, tailSampler, properties);
    }
}
