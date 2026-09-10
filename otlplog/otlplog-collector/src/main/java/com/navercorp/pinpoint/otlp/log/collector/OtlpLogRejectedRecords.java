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

package com.navercorp.pinpoint.otlp.log.collector;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Per-request tally of LogRecords that were not stored, by reason. Feeds both the
 * {@code collector.otlplog.record.dropped{reason}} metric (every reason) and the OTLP
 * {@code ExportLogsPartialSuccess} response (client-visible reasons only, see
 * {@link OtlpLogRejectReason#isClientVisible()}).
 */
public class OtlpLogRejectedRecords {

    private final Map<OtlpLogRejectReason, Long> countByReason = new EnumMap<>(OtlpLogRejectReason.class);

    public void add(OtlpLogRejectReason reason, long count) {
        if (count <= 0) {
            return;
        }
        countByReason.merge(reason, count, Long::sum);
    }

    public void add(OtlpLogRejectReason reason) {
        add(reason, 1);
    }

    public long count(OtlpLogRejectReason reason) {
        return countByReason.getOrDefault(reason, 0L);
    }

    /** Dropped record count per reason; only reasons that occurred are present. */
    public Map<OtlpLogRejectReason, Long> countByReason() {
        return Collections.unmodifiableMap(countByReason);
    }

    /** Records the exporter is told about: the sum over client-visible reasons. */
    public long clientVisibleCount() {
        long total = 0;
        for (Map.Entry<OtlpLogRejectReason, Long> entry : countByReason.entrySet()) {
            if (entry.getKey().isClientVisible()) {
                total += entry.getValue();
            }
        }
        return total;
    }

    /** {@code "no trace context (3), mapping error (1)"} over the client-visible reasons, in enum order. */
    public String clientVisibleMessage() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<OtlpLogRejectReason, Long> entry : countByReason.entrySet()) {
            if (!entry.getKey().isClientVisible()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(entry.getKey().message()).append(" (").append(entry.getValue()).append(')');
        }
        return sb.toString();
    }
}
