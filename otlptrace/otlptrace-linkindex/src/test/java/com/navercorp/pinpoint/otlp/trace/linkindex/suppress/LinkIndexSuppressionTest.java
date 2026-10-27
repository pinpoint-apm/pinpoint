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

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LinkIndexSuppressionTest {

    private static final String MAPMATCH = SuppressKey.of("mapmatch-bg", "navi:12000");
    private static final String GATEWAY_ROOT = SuppressKey.ofServer("sec.gateway.internal", SuppressKey.PARENT_ROOT);
    private final SuppressStore store = mock(SuppressStore.class);

    private LinkIndexSuppression suppression(int probe, String force, String pin) {
        return new LinkIndexSuppression(store, Duration.ofSeconds(60), probe, SuppressKey.parseRules(force), SuppressKey.parseRules(pin));
    }

    @Test
    void nothingSuppressedBeforeTheFirstSnapshot() {
        LinkIndexSuppression suppression = suppression(2, "", "");

        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.getActiveCount()).isZero();
        assertThat(suppression.getSuppressed()).isZero();
    }

    @Test
    void activeIdentifierWritesTheProbeThenSuppresses() {
        when(store.readActive()).thenReturn(Set.of(MAPMATCH));
        LinkIndexSuppression suppression = suppression(2, "", "");
        suppression.refresh();

        assertThat(suppression.getActiveCount()).isEqualTo(1);
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();
        // other destinations of the same application are not affected
        assertThat(suppression.allow("mapmatch-bg", "other:1", 10)).isTrue();
        assertThat(suppression.getProbed()).isEqualTo(2);
        assertThat(suppression.getSuppressed()).isEqualTo(2);

        // a new slot gets a new probe allowance
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 11)).isTrue();
        assertThat(suppression.getProbed()).isEqualTo(3);
    }

    @Test
    void allowIdentifierTakesTheBuiltKey() {
        when(store.readActive()).thenReturn(Set.of(MAPMATCH));
        LinkIndexSuppression suppression = suppression(1, "", "mapmatch-bg|other:1");
        suppression.refresh();

        assertThat(suppression.allowIdentifier("mapmatch-bg", MAPMATCH, 10)).isTrue();     // probe
        assertThat(suppression.allowIdentifier("mapmatch-bg", MAPMATCH, 10)).isFalse();
        // the probe allowance is shared with allow()
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();
        assertThat(suppression.allowIdentifier("mapmatch-bg", SuppressKey.of("mapmatch-bg", "other:1"), 10)).isTrue();
    }

    @Test
    void releasedIdentifierIsWrittenAgainAfterTheNextRefresh() {
        when(store.readActive()).thenReturn(Set.of(MAPMATCH)).thenReturn(Set.of());
        LinkIndexSuppression suppression = suppression(1, "", "");
        suppression.refresh();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();

        suppression.refresh();

        assertThat(suppression.getActiveCount()).isZero();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.getRefreshes()).isEqualTo(2);
    }

    @Test
    void failedRefreshKeepsThePreviousSnapshot() {
        when(store.readActive()).thenReturn(Set.of(MAPMATCH)).thenThrow(new IllegalStateException("store down"));
        LinkIndexSuppression suppression = suppression(1, "", "");
        suppression.refresh();
        suppression.refresh();

        assertThat(suppression.getRefreshErrors()).isEqualTo(1);
        assertThat(suppression.getActiveCount()).isEqualTo(1);
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();
    }

    @Test
    void forcedRulesApplyWithoutTheStore() {
        LinkIndexSuppression suppression = new LinkIndexSuppression(null, Duration.ofSeconds(60), 1,
                SuppressKey.parseRules("mapmatch-bg|navi:12000,sec.gateway.internal"), SuppressKey.Rules.EMPTY);
        suppression.start();   // no store: nothing scheduled
        suppression.refresh();

        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();    // probe
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();
        assertThat(suppression.allow("mapmatch-bg", "other:1", 10)).isTrue();
        // an application rule covers every destination; the probe allowance is still per identifier
        assertThat(suppression.allow("sec.gateway.internal", "coin-api-dev", 10)).isTrue();   // probe
        assertThat(suppression.allow("sec.gateway.internal", "coin-api-dev", 10)).isFalse();
        assertThat(suppression.allow("sec.gateway.internal", "anything", 10)).isTrue();       // probe
        assertThat(suppression.allow("sec.gateway.internal", "anything", 10)).isFalse();
        assertThat(suppression.allow("p.frontend", "cart:7070", 10)).isTrue();
        assertThat(suppression.getRefreshes()).isZero();
        suppression.close();
    }

    @Test
    void pinnedRulesWinOverActiveAndForced() {
        when(store.readActive()).thenReturn(Set.of(MAPMATCH));
        LinkIndexSuppression suppression = suppression(1, "p.frontend", "mapmatch-bg|navi:12000,p.frontend|cart:7070");
        suppression.refresh();

        for (int i = 0; i < 5; i++) {
            assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
            assertThat(suppression.allow("p.frontend", "cart:7070", 10)).isTrue();
        }
        assertThat(suppression.allow("p.frontend", "checkout:5050", 10)).isTrue();    // probe of the forced application
        assertThat(suppression.allow("p.frontend", "checkout:5050", 10)).isFalse();
        assertThat(suppression.getSuppressed()).isEqualTo(1);
    }

    @Test
    void noneAllowsEverything() {
        for (int i = 0; i < 3; i++) {
            assertThat(LinkIndexSuppression.NONE.allow("mapmatch-bg", "navi:12000", i)).isTrue();
        }
        LinkIndexSuppression.NONE.start();
        LinkIndexSuppression.NONE.refresh();
        assertThat(LinkIndexSuppression.NONE.getSuppressed()).isZero();
    }

    @Test
    void activeServerIdentifierWritesTheProbeThenSuppresses() {
        when(store.readActive()).thenReturn(Set.of(GATEWAY_ROOT, MAPMATCH));
        LinkIndexSuppression suppression = suppression(1, "", "");
        suppression.refresh();

        assertThat(suppression.getActiveCount()).isEqualTo(2);
        assertThat(suppression.getActiveServerCount()).isEqualTo(1);
        assertThat(suppression.allowServer("sec.gateway.internal", SuppressKey.PARENT_ROOT, 10)).isTrue();     // probe
        assertThat(suppression.allowServer("sec.gateway.internal", SuppressKey.PARENT_ROOT, 10)).isFalse();
        // the other class of the same application and the CLIENT entries of the application are not affected
        assertThat(suppression.allowServer("sec.gateway.internal", SuppressKey.PARENT_OTHER, 10)).isTrue();
        assertThat(suppression.allow("sec.gateway.internal", "coin-api-dev", 10)).isTrue();
        assertThat(suppression.getProbed(LinkKind.SERVER)).isEqualTo(1);
        assertThat(suppression.getSuppressed(LinkKind.SERVER)).isEqualTo(1);
        assertThat(suppression.getProbed(LinkKind.CLIENT)).isZero();
        assertThat(suppression.getSuppressed(LinkKind.CLIENT)).isZero();
        assertThat(suppression.getSuppressed()).isEqualTo(1);
        assertThat(suppression.getProbed()).isEqualTo(1);
        // the probe allowance is per identifier: the CLIENT identifier still has its own
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();
        assertThat(suppression.getSuppressed(LinkKind.CLIENT)).isEqualTo(1);
    }

    @Test
    void serverDisabledIgnoresTheServerIdentifiersOfTheListAndOfTheForcedRules() {
        when(store.readActive()).thenReturn(Set.of(GATEWAY_ROOT, MAPMATCH));
        LinkIndexSuppression suppression = new LinkIndexSuppression(store, Duration.ofSeconds(60), 1, false,
                SuppressKey.parseRules("S|p.cart"), SuppressKey.Rules.EMPTY);
        suppression.refresh();

        assertThat(suppression.isServerEnabled()).isFalse();
        assertThat(suppression.getActiveCount()).isEqualTo(1);
        assertThat(suppression.getActiveServerCount()).isZero();
        for (int i = 0; i < 3; i++) {
            assertThat(suppression.allowServer("sec.gateway.internal", SuppressKey.PARENT_ROOT, 10)).isTrue();
            assertThat(suppression.allowIdentifier("p.cart", SuppressKey.ofServer("p.cart", SuppressKey.PARENT_OTHER), 10)).isTrue();
        }
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isTrue();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", 10)).isFalse();
        assertThat(suppression.getSuppressed(LinkKind.SERVER)).isZero();
        assertThat(suppression.getProbed(LinkKind.SERVER)).isZero();
        assertThat(suppression.toString()).contains("serverEnabled=false");
    }

    @Test
    void applicationPinCoversTheServerEntriesAndForcedServerRulesApply() {
        when(store.readActive()).thenReturn(Set.of(GATEWAY_ROOT));
        LinkIndexSuppression suppression = suppression(1, "S|p.cart|other", "sec.gateway.internal");
        suppression.refresh();

        for (int i = 0; i < 3; i++) {
            assertThat(suppression.allowServer("sec.gateway.internal", SuppressKey.PARENT_ROOT, 10)).isTrue();    // pinned
        }
        assertThat(suppression.allowServer("p.cart", SuppressKey.PARENT_OTHER, 10)).isTrue();    // probe of the forced identifier
        assertThat(suppression.allowServer("p.cart", SuppressKey.PARENT_OTHER, 10)).isFalse();
        assertThat(suppression.allowServer("p.cart", SuppressKey.PARENT_ROOT, 10)).isTrue();
        assertThat(suppression.getSuppressed(LinkKind.SERVER)).isEqualTo(1);
    }

    @Test
    void invalidSettingsAreRejected() {
        assertThatThrownBy(() -> new LinkIndexSuppression(store, Duration.ofSeconds(60), 0, SuppressKey.Rules.EMPTY, SuppressKey.Rules.EMPTY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LinkIndexSuppression(store, Duration.ZERO, 1, SuppressKey.Rules.EMPTY, SuppressKey.Rules.EMPTY))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
