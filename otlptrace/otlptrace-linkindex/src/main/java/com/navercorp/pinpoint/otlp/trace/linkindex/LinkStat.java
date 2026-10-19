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

package com.navercorp.pinpoint.otlp.trace.linkindex;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.SlotCode;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Call statistics of one {@link LinkKey}: the same columns the collector increments for an InLink row.
 */
public final class LinkStat {

    private final HistogramSchema schema;
    private final Map<SlotCode, Long> histogram = new EnumMap<>(SlotCode.class);
    private long sum;
    private long max;

    public LinkStat(HistogramSchema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    public void add(int elapsed, boolean error, int weight) {
        final HistogramSlot slot = schema.findHistogramSlot(elapsed, error);
        histogram.merge(slot.getSlotCode(), (long) weight, Long::sum);
        sum += (long) elapsed * weight;
        max = Math.max(max, elapsed);
    }

    public void merge(LinkStat other) {
        other.histogram.forEach((code, count) -> histogram.merge(code, count, Long::sum));
        sum += other.sum;
        max = Math.max(max, other.max);
    }

    public Map<SlotCode, Long> getHistogram() {
        return Collections.unmodifiableMap(histogram);
    }

    public long getSum() {
        return sum;
    }

    public long getMax() {
        return max;
    }

    public long getCount() {
        long count = 0;
        for (long value : histogram.values()) {
            count += value;
        }
        return count;
    }
}
