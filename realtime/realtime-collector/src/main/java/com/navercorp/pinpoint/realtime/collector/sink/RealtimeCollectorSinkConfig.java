/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.collector.sink;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
public class RealtimeCollectorSinkConfig {

    @Bean("sinkIdCounter")
    public AtomicLong sinkIdCounter() {
        return new AtomicLong(0);
    }

    @Bean
    public SinkRepository<ActiveThreadCountPublisher> activeThreadCountSinkRepository(@Qualifier("sinkIdCounter") AtomicLong idCounter) {
        return new ActiveThreadCountPublisherSinkRepository(idCounter);
    }

    @Bean
    public SinkRepository<ActiveThreadDumpPublisher> activeThreadDumpSinkRepository(@Qualifier("sinkIdCounter") AtomicLong idCounter) {
        return new SimpleSinkRepository<>(idCounter);
    }

    @Bean
    public SinkRepository<ActiveThreadLightDumpPublisher> activeThreadLightDumpSinkRepository(@Qualifier("sinkIdCounter") AtomicLong idCounter) {
        return new SimpleSinkRepository<>(idCounter);
    }

    @Bean
    public SinkRepository<EchoPublisher> echoSinkRepository(@Qualifier("sinkIdCounter") AtomicLong idCounter) {
        return new SimpleSinkRepository<>(idCounter);
    }
}
