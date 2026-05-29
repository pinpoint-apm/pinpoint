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
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Testcontainers
class TailSamplingEndToEndIT {

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.0")).withExposedPorts(6379);

    // Valid empty serialized PSpan so replay()'s PSpan.parseFrom(...) succeeds.
    private static final byte[] VALID_PROTO = PSpan.newBuilder().build().toByteArray();

    private LettuceConnectionFactory factory;
    private TraceService always;
    private TraceService sampled;
    private TailSampler tailSampler;

    @BeforeEach
    void setUp() throws Exception {
        factory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        factory.afterPropertiesSet();
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();
        template.execute((RedisCallback<Object>) c -> {
            c.serverCommands().flushDb();
            return null;
        });

        byte[] acceptScript = StreamUtils.copyToByteArray(new ClassPathResource("redis/tail-accept.lua").getInputStream());
        byte[] decideScript = StreamUtils.copyToByteArray(new ClassPathResource("redis/tail-decide.lua").getInputStream());
        TailSamplingRepository repository = new TailSamplingRepository(template, acceptScript, decideScript, 300, 600);

        always = Mockito.mock(StatisticsTraceService.class);
        sampled = Mockito.mock(TraceService.class);
        GrpcSpanFactory spanFactory = Mockito.mock(GrpcSpanFactory.class);
        Mockito.when(spanFactory.buildSpanBo(any(), any(), Mockito.anyLong())).thenReturn(new SpanBo());

        TailSamplingProperties props = new TailSamplingProperties();
        TailSamplingProperties.Band fast = new TailSamplingProperties.Band();
        fast.setMaxElapsed(java.time.Duration.ofMillis(50));
        fast.setRate(0); // fast traces 0% (deterministic drop for the test)
        TailSamplingProperties.Band slow = new TailSamplingProperties.Band();
        slow.setRate(100); // catch-all keep
        props.setBands(List.of(fast, slow));

        tailSampler = new TailSampler(new TraceService[]{always, sampled},
                repository, props, new BufferedSpanCodec(), spanFactory, new SimpleMeterRegistry());
    }

    @AfterEach
    void tearDown() {
        factory.destroy();
    }

    private SpanBo span(long seq, int elapsed, boolean root) {
        SpanBo bo = new SpanBo();
        bo.setTransactionId(new PinpointServerTraceId("agent", 1L, seq));
        bo.setParentSpanId(root ? -1L : 10L);
        bo.setElapsed(elapsed);
        bo.setAgentId("agent");
        bo.setApplicationName("app");
        bo.setAgentName("agentName");
        return bo;
    }

    @Test
    void slowTrace_childBeforeRoot_allKept() {
        // child first (buffered), then root with elapsed 500 -> catch-all 100% keep
        tailSampler.acceptSpan(span(1, 0, false), VALID_PROTO);
        tailSampler.acceptSpan(span(1, 500, true), VALID_PROTO);
        // root triggers decide(keep) -> both buffered spans replayed to sampled
        verify(sampled, Mockito.atLeast(1)).insertSpan(any());
    }

    @Test
    void fastTrace_dropped() {
        // root first, elapsed 10 -> fast band rate 0 -> drop
        tailSampler.acceptSpan(span(2, 10, true), VALID_PROTO);
        // later child sees decision=drop -> discarded immediately
        tailSampler.acceptSpan(span(2, 0, false), VALID_PROTO);
        verify(sampled, never()).insertSpan(any());
        verify(always, Mockito.times(2)).insertSpan(any()); // always group called for every span
    }
}
