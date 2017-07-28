/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.active;


import com.google.common.primitives.Ints;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultActiveTraceHistogram implements ActiveTraceHistogram {
    static final int SLOT_SIZE = 4;

    private final HistogramSchema histogramSchema;
    private int fast;
    private int normal;
    private int slow;
    private int verySlow;

    public DefaultActiveTraceHistogram(HistogramSchema histogramSchema) {
        this.histogramSchema = Assert.requireNonNull(histogramSchema, "histogramSchema must not be null");
    }

    public void increment(HistogramSlot slot) {
        Assert.requireNonNull(slot, "slot must not be null");

        final SlotType slotType = slot.getSlotType();
        switch (slotType) {
            case FAST:
                this.fast++;
                return;
            case NORMAL:
                this.normal++;
                return;
            case SLOW:
                this.slow++;
                return;
            case VERY_SLOW:
                this.verySlow++;
                return;
            default:
                throw new UnsupportedOperationException("slot type:" + slot);
        }
    }

    @Override
    public HistogramSchema getHistogramSchema() {
        return histogramSchema;
    }

    @Override
    public List<Integer> getActiveTraceCounts() {
        return Ints.asList(fast, normal, slow, verySlow);
    }


}
