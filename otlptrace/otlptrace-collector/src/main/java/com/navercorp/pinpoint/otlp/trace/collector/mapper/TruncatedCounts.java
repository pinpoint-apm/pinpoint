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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

/**
 * Per-label counts of span data truncated by the collector, for the
 * OPENTELEMETRY_TRUNCATED annotation value. Each category is incremented through a no-arg
 * method so it can be handed out as a {@link TruncationListener} method reference
 * (e.g. {@code truncatedCounts::attribute}). {@link #toString()} formats the counts
 * as space-separated {@code label=count} pairs in declaration order
 * (e.g. {@code "attributes=3 sql=1"}); zero counts are omitted.
 */
final class TruncatedCounts {

    private int attributes;
    private int sql;
    private int events;
    private int links;

    void attribute() {
        attributes++;
    }

    void sql() {
        sql++;
    }

    void event() {
        events++;
    }

    void link() {
        links++;
    }

    boolean isEmpty() {
        return attributes == 0 && sql == 0 && events == 0 && links == 0;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        append(buffer, "attributes", attributes);
        append(buffer, "sql", sql);
        append(buffer, "events", events);
        append(buffer, "links", links);
        return buffer.toString();
    }

    private static void append(StringBuilder buffer, String label, int count) {
        if (count > 0) {
            if (!buffer.isEmpty()) {
                buffer.append(' ');
            }
            buffer.append(label).append('=').append(count);
        }
    }
}
