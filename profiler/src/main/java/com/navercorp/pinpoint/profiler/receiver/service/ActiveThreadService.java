/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.SlotType;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceInfo;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThread;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadResponse;
import org.apache.thrift.TBase;

import java.util.*;

/**
 * @author Taejin Koo
 */
public class ActiveThreadService implements ProfilerRequestCommandService {

    private static final List<SlotType> ACTIVE_THREAD_SLOTS_ORDER = new ArrayList<SlotType>();
    static {
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.FAST);
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.NORMAL);
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.SLOW);
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.VERY_SLOW);
        ACTIVE_THREAD_SLOTS_ORDER.add(SlotType.ERROR);
    }

    private final ActiveTraceLocator activeTraceLocator;
    private final int activeThreadSlotsCount;
    private final HistogramSchema histogramSchema = HistogramSchema.NORMAL_SCHEMA;

    public ActiveThreadService(ActiveTraceLocator activeTraceLocator) {
        if (activeTraceLocator == null) {
            throw new NullPointerException("activeTraceLocator");
        }
        this.activeTraceLocator = activeTraceLocator;
        this.activeThreadSlotsCount = ACTIVE_THREAD_SLOTS_ORDER.size();
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase tBase) {
        if (tBase == null || !(tBase instanceof TActiveThread)) {
            TResult fail = new TResult();
            fail.setSuccess(false);
            fail.setMessage("Expected object type error. expected:" + getCommandClazz() + ", but was:" + ClassUtils.simpleClassName(tBase));
            return fail;
        }

        Map<SlotType, IntAdder> mappedSlot = new LinkedHashMap<SlotType, IntAdder>(activeThreadSlotsCount);
        for (SlotType slotType : ACTIVE_THREAD_SLOTS_ORDER) {
            mappedSlot.put(slotType, new IntAdder(0));
        }

        long currentTime = System.currentTimeMillis();

        List<ActiveTraceInfo> activeTraceInfoCollect = activeTraceLocator.collect();
        for (ActiveTraceInfo activeTraceInfo : activeTraceInfoCollect) {
            HistogramSlot slot = histogramSchema.findHistogramSlot((int) (System.currentTimeMillis() - activeTraceInfo.getStartTime()));
            mappedSlot.get(slot.getSlotType()).incrementAndGet();
        }

        List<Integer> activeThreadCount = new ArrayList<Integer>(activeThreadSlotsCount);
        for (IntAdder statusCount : mappedSlot.values()) {
            activeThreadCount.add(statusCount.get());
        }

        TActiveThreadResponse response = new TActiveThreadResponse();
        response.setHistogramSchemaType(histogramSchema.getTypeCode());
        response.setActiveThreadCount(activeThreadCount);

        return response;
    }

    @Override
    public Class<? extends TBase> getCommandClazz() {
        return TActiveThread.class;
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

}
