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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class TailSamplingRepositoryIT {

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7.0")).withExposedPorts(6379);

    private LettuceConnectionFactory factory;
    private TailSamplingRepository repository;

    @BeforeEach
    void setUp() {
        factory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
        factory.afterPropertiesSet();
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();

        byte[] acceptScript = load("redis/tail-accept.lua");
        byte[] decideScript = load("redis/tail-decide.lua");
        repository = new TailSamplingRepository(template, acceptScript, decideScript, 300, 600);
        template.execute((org.springframework.data.redis.core.RedisCallback<Object>) c -> {
            c.serverCommands().flushDb();
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        factory.destroy();
    }

    private static byte[] load(String path) {
        try {
            return org.springframework.util.StreamUtils.copyToByteArray(
                    new org.springframework.core.io.ClassPathResource(path).getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void accept_firstTime_returnsBuffered() {
        String r = repository.accept("tx-1", new byte[]{1}, 1000L, false);
        assertThat(r).isEqualTo("buffered");
    }

    @Test
    void decide_keep_returnsBufferedSpansAndClearsBuffer() {
        repository.accept("tx-2", new byte[]{1}, 1000L, false);
        repository.accept("tx-2", new byte[]{2}, 1000L, false);

        List<byte[]> spans = repository.decide("tx-2", true);

        assertThat(spans).hasSize(2);
        assertThat(repository.accept("tx-2", new byte[]{3}, 1000L, false)).isEqualTo("keep");
    }

    @Test
    void decide_drop_returnsEmptyAndSubsequentAcceptDrops() {
        repository.accept("tx-3", new byte[]{1}, 1000L, false);
        List<byte[]> spans = repository.decide("tx-3", false);
        assertThat(spans).isEmpty();
        assertThat(repository.accept("tx-3", new byte[]{9}, 1000L, false)).isEqualTo("drop");
    }

    @Test
    void decide_secondCall_returnsNull_noDoubleFlush() {
        repository.accept("tx-4", new byte[]{1}, 1000L, false);
        assertThat(repository.decide("tx-4", true)).isNotNull();
        assertThat(repository.decide("tx-4", true)).isNull();
    }

    @Test
    void findStale_returnsTxidsOlderThanThreshold() {
        repository.accept("tx-old", new byte[]{1}, 1000L, false);
        repository.accept("tx-new", new byte[]{1}, 5000L, false);
        List<String> stale = repository.findStale(2000L, 100);
        assertThat(stale).contains("tx-old").doesNotContain("tx-new");
    }

    @Test
    void decide_proposedDrop_upgradedToKeep_whenErrorFlagSet() {
        // a child span (no error) buffered, then an errored span sets the error flag
        repository.accept("tx-err", new byte[]{1}, 1000L, false);
        repository.accept("tx-err", new byte[]{2}, 1000L, true);

        // band proposes drop, but the error flag forces keep -> spans returned
        List<byte[]> spans = repository.decide("tx-err", false);

        assertThat(spans).hasSize(2);
        // decision stored as keep -> late spans write through
        assertThat(repository.accept("tx-err", new byte[]{3}, 1000L, false)).isEqualTo("keep");
    }

    @Test
    void decide_proposedDrop_staysDrop_whenNoErrorFlag() {
        repository.accept("tx-nor", new byte[]{1}, 1000L, false);
        List<byte[]> spans = repository.decide("tx-nor", false);
        assertThat(spans).isEmpty();
        assertThat(repository.accept("tx-nor", new byte[]{9}, 1000L, false)).isEqualTo("drop");
    }

    @Test
    void isErrorFlagged_reflectsErrorAccept() {
        repository.accept("tx-flag-no", new byte[]{1}, 1000L, false);
        assertThat(repository.isErrorFlagged("tx-flag-no")).isFalse();

        repository.accept("tx-flag-yes", new byte[]{1}, 1000L, true);
        assertThat(repository.isErrorFlagged("tx-flag-yes")).isTrue();
    }

    @Test
    void defer_findDue_remove_roundTrip() {
        repository.defer("tx-def-old", 1000L);
        repository.defer("tx-def-new", 5000L);

        List<String> due = repository.findDeferredDue(2000L, 100);
        assertThat(due).contains("tx-def-old").doesNotContain("tx-def-new");

        repository.removeDeferred("tx-def-old");
        assertThat(repository.findDeferredDue(2000L, 100)).doesNotContain("tx-def-old");
    }

    @Test
    void deferredTrace_finalizedAsKeep_whenErrorArrivesDuringGrace() {
        // root buffered (band would drop) -> deferred
        repository.accept("tx-grace", new byte[]{1}, 1000L, false);
        repository.defer("tx-grace", 1000L);
        // a downstream error span arrives during the grace window
        repository.accept("tx-grace", new byte[]{2}, 1000L, true);

        // finalize with proposed drop -> error flag upgrades to keep
        List<byte[]> won = repository.decide("tx-grace", false);
        assertThat(won).hasSize(2);
    }
}
