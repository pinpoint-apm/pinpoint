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

class SuppressStateCodecTest {

    @Test
    void roundTrip() {
        SuppressState candidate = SuppressState.candidate(29_877_190L, 7_596);
        SuppressState active = candidate.seen(29_877_191L, 8_000, true).activated(29_878_630L);

        assertThat(SuppressStateCodec.decode(SuppressStateCodec.encode(candidate))).isEqualTo(candidate);
        assertThat(SuppressStateCodec.decode(SuppressStateCodec.encode(active))).isEqualTo(active);
        assertThat(active.isActive()).isTrue();
        assertThat(active.unmatchedSlots()).isEqualTo(2);
        assertThat(active.unmatched()).isEqualTo(15_596);
        assertThat(active.lastSeenSlot()).isEqualTo(29_877_191L);
        assertThat(active.activatedSlot()).isEqualTo(29_878_630L);
    }

    @Test
    void seenWithoutCountingTheSlot() {
        SuppressState state = SuppressState.candidate(10, 500).seen(12, 3, false);

        assertThat(state.unmatchedSlots()).isEqualTo(1);
        assertThat(state.unmatched()).isEqualTo(503);
        assertThat(state.lastSeenSlot()).isEqualTo(12);
    }

    @Test
    void lastSeenNeverGoesBackwards() {
        SuppressState state = SuppressState.candidate(10, 500).seen(8, 300, true);

        assertThat(state.lastSeenSlot()).isEqualTo(10);
        assertThat(state.unmatchedSlots()).isEqualTo(2);
    }

    @Test
    void invalidValuesAreRejected() {
        assertThatThrownBy(() -> SuppressStateCodec.decode(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SuppressStateCodec.decode(new byte[]{1})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SuppressStateCodec.decode(new byte[]{9, 1, 0, 0, 0, 0, 0}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("version");
        assertThatThrownBy(() -> SuppressStateCodec.decode(new byte[]{1, 7, 0, 0, 0, 0, 0}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("state");
        assertThatThrownBy(() -> SuppressStateCodec.decode(new byte[]{1, 1, 0}))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Truncated");
    }
}
