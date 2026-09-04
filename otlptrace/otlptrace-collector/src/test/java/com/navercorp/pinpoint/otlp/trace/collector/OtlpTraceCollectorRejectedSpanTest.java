/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OtlpTraceCollectorRejectedSpanTest {

    @Test
    void addCount_accumulatesTotalAndPerReason() {
        OtlpTraceCollectorRejectedSpan rejected = new OtlpTraceCollectorRejectedSpan();

        rejected.addCount(OtlpTraceRejectReason.INVALID_ID, 2);
        rejected.addCount(OtlpTraceRejectReason.ORPHAN, 3);
        rejected.addCount(OtlpTraceRejectReason.INVALID_ID, 1);

        assertThat(rejected.count()).isEqualTo(6);
        assertThat(rejected.count(OtlpTraceRejectReason.INVALID_ID)).isEqualTo(3);
        assertThat(rejected.count(OtlpTraceRejectReason.ORPHAN)).isEqualTo(3);
        assertThat(rejected.count(OtlpTraceRejectReason.UNSAMPLED)).isZero();
        assertThat(rejected.countByReason()).containsOnly(
                Map.entry(OtlpTraceRejectReason.INVALID_ID, 3L),
                Map.entry(OtlpTraceRejectReason.ORPHAN, 3L));
    }

    @Test
    void addCount_zeroOrNegative_isIgnored() {
        OtlpTraceCollectorRejectedSpan rejected = new OtlpTraceCollectorRejectedSpan();

        rejected.addCount(OtlpTraceRejectReason.MAPPING_ERROR, 0);
        rejected.addCount(OtlpTraceRejectReason.MAPPING_ERROR, -1);

        assertThat(rejected.count()).isZero();
        assertThat(rejected.countByReason()).isEmpty();
    }

    @Test
    void countByReason_isReadOnly() {
        OtlpTraceCollectorRejectedSpan rejected = new OtlpTraceCollectorRejectedSpan();
        rejected.addCount(OtlpTraceRejectReason.UNSAMPLED, 1);

        assertThatThrownBy(() -> rejected.countByReason().put(OtlpTraceRejectReason.ORPHAN, 1L))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getMessage_joinsMessagesUnchangedByReasonTracking() {
        // The partial-success errorMessage format is a client-facing contract; reason tracking must
        // not alter it.
        OtlpTraceCollectorRejectedSpan rejected = new OtlpTraceCollectorRejectedSpan();
        rejected.putMessage(OtlpTraceRejectReason.INVALID_ID.message() + " (2)");
        rejected.addCount(OtlpTraceRejectReason.INVALID_ID, 2);
        rejected.putMessage("invalid pinpoint.agentId=bad id (1)");
        rejected.addCount(OtlpTraceRejectReason.INVALID_RESOURCE, 1);

        assertThat(rejected.getMessage()).isEqualTo("invalid id (2), invalid pinpoint.agentId=bad id (1)");
        assertThat(rejected.count()).isEqualTo(3);
    }

    @Test
    void reasonTagValues_areStableSnakeCase() {
        // Documented in the README and used as the metric tag: a rename is a dashboard change.
        assertThat(OtlpTraceRejectReason.UNSAMPLED.tagValue()).isEqualTo("unsampled");
        assertThat(OtlpTraceRejectReason.INVALID_ID.tagValue()).isEqualTo("invalid_id");
        assertThat(OtlpTraceRejectReason.INVALID_RESOURCE.tagValue()).isEqualTo("invalid_resource");
        assertThat(OtlpTraceRejectReason.ORPHAN.tagValue()).isEqualTo("orphan");
        assertThat(OtlpTraceRejectReason.MAPPING_ERROR.tagValue()).isEqualTo("mapping_error");
    }
}
