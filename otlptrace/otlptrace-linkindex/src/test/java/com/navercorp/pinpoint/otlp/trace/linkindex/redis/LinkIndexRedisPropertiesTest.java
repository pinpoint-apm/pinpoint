/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.linkindex.redis;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinkIndexRedisPropertiesTest {

    private static LinkIndexRedisProperties properties(String mode, String nodes, String password) {
        return new LinkIndexRedisProperties(mode, nodes, password, Duration.ofSeconds(1), Duration.ofSeconds(1));
    }

    @Test
    void modeDefaultsToCluster() {
        assertThat(properties(null, "a:1", null).parseMode()).isEqualTo(LettuceLinkIndexRedisConnector.Mode.CLUSTER);
        assertThat(properties("", "a:1", null).parseMode()).isEqualTo(LettuceLinkIndexRedisConnector.Mode.CLUSTER);
        assertThat(properties("Standalone", "a:1", null).parseMode()).isEqualTo(LettuceLinkIndexRedisConnector.Mode.STANDALONE);
    }

    @Test
    void nodes() {
        assertThat(properties("cluster", " a:1, b:2 ,,c:3 ", null).parseNodes()).containsExactly("a:1", "b:2", "c:3");
        assertThat(properties("cluster", "", null).hasNodes()).isFalse();
        assertThat(properties("cluster", null, null).hasNodes()).isFalse();
    }

    @Test
    void passwordIsNotPrinted() {
        assertThat(properties("cluster", "a:1", "secret").toString()).doesNotContain("secret").contains("****");
    }

    @Test
    void invalidNode() {
        assertThatThrownBy(() -> LettuceLinkIndexRedisConnector.toUri("no-port", null, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LettuceLinkIndexRedisConnector(LettuceLinkIndexRedisConnector.Mode.STANDALONE,
                List.of("a:1", "b:2"), null, Duration.ofSeconds(1), Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
