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

package com.navercorp.pinpoint.otlp.trace.linkindex.suppress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuppressKeyTest {

    @Test
    void keyRoundTrip() {
        String key = SuppressKey.of("mapmatch-bg", "navi-bg-grpc.directions.svc:12000");

        assertThat(SuppressKey.applicationName(key)).isEqualTo("mapmatch-bg");
        assertThat(SuppressKey.destinationId(key)).isEqualTo("navi-bg-grpc.directions.svc:12000");
        assertThat(SuppressKey.display(key)).isEqualTo("mapmatch-bg|navi-bg-grpc.directions.svc:12000");
    }

    @Test
    void destinationMayContainTheDisplaySeparator() {
        String key = SuppressKey.of("app", "host|odd:80");

        assertThat(SuppressKey.applicationName(key)).isEqualTo("app");
        assertThat(SuppressKey.destinationId(key)).isEqualTo("host|odd:80");
    }

    @Test
    void parseRules() {
        SuppressKey.Rules rules = SuppressKey.parseRules(" mapmatch-bg | navi:12000 , sec.gateway.internal ,, ");

        assertThat(rules.applications()).containsExactly("sec.gateway.internal");
        assertThat(rules.keys()).containsExactly(SuppressKey.of("mapmatch-bg", "navi:12000"));
        assertThat(rules.matches("mapmatch-bg", SuppressKey.of("mapmatch-bg", "navi:12000"))).isTrue();
        assertThat(rules.matches("mapmatch-bg", SuppressKey.of("mapmatch-bg", "other:1"))).isFalse();
        assertThat(rules.matches("sec.gateway.internal", SuppressKey.of("sec.gateway.internal", "any"))).isTrue();
        assertThat(rules.toString()).contains("mapmatch-bg|navi:12000").contains("sec.gateway.internal");
    }

    @Test
    void emptyRules() {
        assertThat(SuppressKey.parseRules(null)).isSameAs(SuppressKey.Rules.EMPTY);
        assertThat(SuppressKey.parseRules("  ,  ").isEmpty()).isTrue();
        assertThat(SuppressKey.Rules.EMPTY.matches("a", "b")).isFalse();
    }

    @Test
    void invalidRuleIsRejected() {
        assertThatThrownBy(() -> SuppressKey.parseRules("app|"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("app|");
        assertThatThrownBy(() -> SuppressKey.parseRules("|dest"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
