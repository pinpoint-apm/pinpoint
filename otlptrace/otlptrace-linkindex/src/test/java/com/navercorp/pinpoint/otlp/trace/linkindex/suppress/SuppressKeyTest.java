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

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntry;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;
import org.junit.jupiter.api.Test;

import java.util.Set;

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

    @Test
    void serverKeyRoundTrip() {
        String key = SuppressKey.ofServer("sec.gateway.internal", SuppressKey.PARENT_ROOT);

        assertThat(SuppressKey.isServer(key)).isTrue();
        assertThat(SuppressKey.kind(key)).isEqualTo(LinkKind.SERVER);
        assertThat(SuppressKey.applicationName(key)).isEqualTo("sec.gateway.internal");
        assertThat(SuppressKey.destinationId(key)).isNull();
        assertThat(SuppressKey.parentClass(key)).isEqualTo("root");
        assertThat(SuppressKey.display(key)).isEqualTo("server:sec.gateway.internal|root");

        String client = SuppressKey.of("S", "sec.gateway.internal");    // an application literally named S
        assertThat(SuppressKey.isServer(client)).isFalse();
        assertThat(SuppressKey.kind(client)).isEqualTo(LinkKind.CLIENT);
        assertThat(SuppressKey.parentClass(client)).isNull();
        assertThat(client).isNotEqualTo(key);
        assertThat(SuppressKey.display(client)).isEqualTo("S|sec.gateway.internal");
    }

    @Test
    void clientRulesOfAnApplicationNamedSAreUnchanged() {
        // "S|cart:7070" is the CLIENT identifier of application S, not a SERVER rule: SERVER rules start with "server:"
        SuppressKey.Rules rules = SuppressKey.parseRules("S|cart:7070, S");

        assertThat(rules.keys()).containsExactly(SuppressKey.of("S", "cart:7070"));
        assertThat(rules.applications()).containsExactly("S");
        assertThat(rules.serverApplications()).isEmpty();
        assertThat(rules.matches("S", SuppressKey.of("S", "cart:7070"))).isTrue();
        assertThat(rules.matches("cart:7070", SuppressKey.ofServer("cart:7070", "root"))).isFalse();
    }

    @Test
    void serverClassIsRootWhenTheParentSpanIdIsTheLowHalfOfTheTraceId() {
        byte[] traceId = new byte[16];
        for (int i = 0; i < 16; i++) {
            traceId[i] = (byte) (0x10 + i);
        }
        long low = 0x18191a1b1c1d1e1fL;

        assertThat(SuppressKey.serverClass(traceId, low)).isEqualTo(SuppressKey.PARENT_ROOT);
        assertThat(SuppressKey.serverClass(traceId, low + 1)).isEqualTo(SuppressKey.PARENT_OTHER);
        assertThat(SuppressKey.serverClass(traceId, 0x1011121314151617L)).isEqualTo(SuppressKey.PARENT_OTHER);  // the high half
        assertThatThrownBy(() -> SuppressKey.serverClass(new byte[8], low)).isInstanceOf(IllegalArgumentException.class);

        assertThat(SuppressKey.of(LinkEntry.server(traceId, low, 1, "gw", 1400, 1, false)))
                .isEqualTo(SuppressKey.ofServer("gw", SuppressKey.PARENT_ROOT));
        assertThat(SuppressKey.of(LinkEntry.server(traceId, 7, 1, "gw", 1400, 1, false)))
                .isEqualTo(SuppressKey.ofServer("gw", SuppressKey.PARENT_OTHER));
        assertThat(SuppressKey.of(LinkEntry.client(traceId, 7, 1, "gw", 1400, "cart:7070")))
                .isEqualTo(SuppressKey.of("gw", "cart:7070"));
    }

    @Test
    void parseServerRules() {
        SuppressKey.Rules rules = SuppressKey.parseRules("server:sec.gateway.internal|root, server: p.cart , mapmatch-bg|navi:12000, p.frontend");

        assertThat(rules.keys()).containsExactlyInAnyOrder(
                SuppressKey.ofServer("sec.gateway.internal", "root"), SuppressKey.of("mapmatch-bg", "navi:12000"));
        assertThat(rules.serverApplications()).containsExactly("p.cart");
        assertThat(rules.applications()).containsExactly("p.frontend");
        // one SERVER identifier
        assertThat(rules.matches("sec.gateway.internal", SuppressKey.ofServer("sec.gateway.internal", "root"))).isTrue();
        assertThat(rules.matches("sec.gateway.internal", SuppressKey.ofServer("sec.gateway.internal", "other"))).isFalse();
        assertThat(rules.matches("sec.gateway.internal", SuppressKey.of("sec.gateway.internal", "root"))).isFalse();
        // every SERVER entry of an application, but not its CLIENT entries
        assertThat(rules.matches("p.cart", SuppressKey.ofServer("p.cart", "other"))).isTrue();
        assertThat(rules.matches("p.cart", SuppressKey.of("p.cart", "db:3306"))).isFalse();
        // an application rule covers both kinds
        assertThat(rules.matches("p.frontend", SuppressKey.ofServer("p.frontend", "root"))).isTrue();
        assertThat(rules.matches("p.frontend", SuppressKey.of("p.frontend", "cart:7070"))).isTrue();
        assertThat(rules.toString()).contains("server:sec.gateway.internal|root").contains("server:p.cart");
        assertThat(new SuppressKey.Rules(Set.of("a"), Set.of()).serverApplications()).isEmpty();
    }

    @Test
    void invalidServerRuleIsRejected() {
        assertThatThrownBy(() -> SuppressKey.parseRules("server:"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SuppressKey.parseRules("server:app|client"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("root");
    }
}
