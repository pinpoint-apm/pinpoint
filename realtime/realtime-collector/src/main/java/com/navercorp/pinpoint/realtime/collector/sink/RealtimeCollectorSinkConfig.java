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

import com.google.common.cache.CacheBuilder;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.MonoSink;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
public class RealtimeCollectorSinkConfig {

    @Value("${pinpoint.modules.realtime.sink.guava.enabled:false}")
    private boolean enableGuava;

    @Value("${pinpoint.modules.realtime.sink.guava.max-size:65536}")
    private long maxGuavaCacheSize;

    @Bean("sinkIdCounter")
    public AtomicLong sinkIdCounter() {
        return new AtomicLong(0);
    }

    @Bean
    public SinkRepository<FluxSink<PCmdActiveThreadCountRes>> activeThreadCountSinkRepository(
            @Qualifier("sinkIdCounter") AtomicLong idCounter
    ) {
        return this.buildSink(idCounter);
    }

    @Bean
    public SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> activeThreadDumpSinkRepository(
            @Qualifier("sinkIdCounter") AtomicLong idCounter
    ) {
        return this.buildSink(idCounter);
    }

    @Bean
    public SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> activeThreadLightDumpSinkRepository(
            @Qualifier("sinkIdCounter") AtomicLong idCounter
    ) {
        return this.buildSink(idCounter);
    }

    @Bean
    public SinkRepository<MonoSink<PCmdEchoResponse>> echoSinkRepository(
            @Qualifier("sinkIdCounter") AtomicLong idCounter
    ) {
        return this.buildSink(idCounter);
    }

    private <T> SinkRepository<T> buildSink(AtomicLong idCounter) {
        if (this.enableGuava) {
            return this.buildGuavaSink(idCounter);
        } else {
            return new SimpleSinkRepository<>(idCounter);
        }
    }

    private <T> SinkRepository<T> buildGuavaSink(AtomicLong idCounter) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(this.maxGuavaCacheSize)
                .weakKeys();
        return new GuavaSinkRepository<>(cacheBuilder, idCounter);
    }

    @Bean
    public ErrorSinkRepository commandErrorSinkRepository(
            List<SinkRepository<MonoSink<?>>> monoSinkRepositories,
            List<SinkRepository<FluxSink<?>>> fluxSinkRepositories
    ) {
        return new IterativeErrorSinkRepository(monoSinkRepositories, fluxSinkRepositories);
    }

}
