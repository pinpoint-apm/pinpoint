/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.active;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.SlotType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 * @author HyunGil Jeong
 */
public class ActiveTraceHistogramFactory {

    private final ActiveTraceRepository activeTraceRepository;
    private final int activeTraceSlotsCount;
    private final HistogramSchema histogramSchema = BaseHistogramSchema.NORMAL_SCHEMA;

    private static final List<SlotType> ACTIVE_TRACE_SLOTS_ORDER = new ArrayList<SlotType>();

    static {
        ACTIVE_TRACE_SLOTS_ORDER.add(SlotType.FAST);
        ACTIVE_TRACE_SLOTS_ORDER.add(SlotType.NORMAL);
        ACTIVE_TRACE_SLOTS_ORDER.add(SlotType.SLOW);
        ACTIVE_TRACE_SLOTS_ORDER.add(SlotType.VERY_SLOW);
    }

    public ActiveTraceHistogramFactory(ActiveTraceRepository activeTraceRepository) {
        if (activeTraceRepository == null) {
            throw new NullPointerException("activeTraceRepository must not be null");
        }
        this.activeTraceRepository = activeTraceRepository;
        this.activeTraceSlotsCount = ACTIVE_TRACE_SLOTS_ORDER.size();
    }

    public ActiveTraceHistogram createHistogram() {
        Map<SlotType, IntAdder> mappedSlot = new LinkedHashMap<SlotType, IntAdder>(activeTraceSlotsCount);
        for (SlotType slotType : ACTIVE_TRACE_SLOTS_ORDER) {
            mappedSlot.put(slotType, new IntAdder(0));
        }

        long currentTime = System.currentTimeMillis();

        List<ActiveTraceInfo> collectedActiveTraceInfo = activeTraceRepository.collect();
        for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
            HistogramSlot slot = histogramSchema.findHistogramSlot((int) (currentTime - activeTraceInfo.getStartTime()), false);
            mappedSlot.get(slot.getSlotType()).incrementAndGet();
        }

        List<Integer> activeTraceCount = new ArrayList<Integer>(activeTraceSlotsCount);
        for (IntAdder statusCount : mappedSlot.values()) {
            activeTraceCount.add(statusCount.get());
        }
        return new ActiveTraceHistogram(this.histogramSchema, activeTraceCount);
    }

    private static class IntAdder {
        private int value = 0;

        public IntAdder(int defaultValue) {
            this.value = defaultValue;
        }

        public int incrementAndGet() {
            return ++value;
        }

        public int get() {
            return this.value;
        }
    }

    public static class ActiveTraceHistogram {

        private final HistogramSchema histogramSchema;
        private final List<Integer> activeTraceCounts;

        private ActiveTraceHistogram(HistogramSchema histogramSchema, List<Integer> activeTraceCounts) {
            this.histogramSchema = histogramSchema;
            if (activeTraceCounts == null) {
                this.activeTraceCounts = Collections.emptyList();
            } else {
                this.activeTraceCounts = activeTraceCounts;
            }
        }

        public HistogramSchema getHistogramSchema() {
            return histogramSchema;
        }

        public List<Integer> getActiveTraceCounts() {
            return activeTraceCounts;
        }
    }
}
