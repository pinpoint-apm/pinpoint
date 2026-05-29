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

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class TailDecisionsTest {

    @Test
    void rate100_alwaysKeep() {
        assertThat(TailDecisions.keep("agent^1^1", 100)).isTrue();
        assertThat(TailDecisions.keep("agent^1^999999", 100)).isTrue();
    }

    @Test
    void rate0_neverKeep() {
        assertThat(TailDecisions.keep("agent^1^1", 0)).isFalse();
        assertThat(TailDecisions.keep("agent^1^999999", 0)).isFalse();
    }

    @Test
    void deterministicForSameTxid() {
        boolean first = TailDecisions.keep("agent^1^42", 10);
        for (int i = 0; i < 100; i++) {
            assertThat(TailDecisions.keep("agent^1^42", 10)).isEqualTo(first);
        }
    }

    @Test
    void approximatesRateAcrossManyTxids() {
        int kept = 0;
        int total = 10000;
        for (int i = 0; i < total; i++) {
            if (TailDecisions.keep("agent^1^" + i, 10)) {
                kept++;
            }
        }
        // 10% ± 2%p
        assertThat(kept).isBetween((int) (total * 0.08), (int) (total * 0.12));
    }
}
