/*
 * Copyright 2024 NAVER Corp.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Per-request tally of spans rejected during mapping (client-side data faults). The total and the
 * message list feed the OTLP {@code ExportTracePartialSuccess} response; the per-reason counts feed
 * the {@code collector.otlptrace.span.rejected{reason}} metric so the same rejections are visible
 * as a server-side time series and not only in the client's response.
 */
public class OtlpTraceCollectorRejectedSpan {
    private long count;
    private final Map<OtlpTraceRejectReason, Long> countByReason = new EnumMap<>(OtlpTraceRejectReason.class);
    private final List<String> messageList = new ArrayList<>();

    public long count() {
        return count;
    }

    public void addCount(OtlpTraceRejectReason reason, long count) {
        if (count <= 0) {
            return;
        }
        this.count += count;
        this.countByReason.merge(reason, count, Long::sum);
    }

    public long count(OtlpTraceRejectReason reason) {
        return countByReason.getOrDefault(reason, 0L);
    }

    /** Rejected span count per reason; only reasons that occurred are present. */
    public Map<OtlpTraceRejectReason, Long> countByReason() {
        return Collections.unmodifiableMap(countByReason);
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (String message : messageList) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(message);
        }

        return sb.toString();
    }

    public void putMessage(String message) {
        messageList.add(message);
    }
}
