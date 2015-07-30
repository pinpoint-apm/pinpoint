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

import com.navercorp.pinpoint.profiler.context.ActiveTraceInfo;
import com.navercorp.pinpoint.profiler.context.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.rpc.util.ClassUtils;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThread;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadResponse;
import org.apache.thrift.TBase;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class ActiveThreadService implements ProfilerRequestCommandService {

    private static final List<ActiveThreadStatus> ACTIVE_THREAD_STATUSES_ORDER = new ArrayList<ActiveThreadStatus>();
    static {
        ACTIVE_THREAD_STATUSES_ORDER.add(ActiveThreadStatus.FAST);
        ACTIVE_THREAD_STATUSES_ORDER.add(ActiveThreadStatus.NORMAL);
        ACTIVE_THREAD_STATUSES_ORDER.add(ActiveThreadStatus.SLOW);
        ACTIVE_THREAD_STATUSES_ORDER.add(ActiveThreadStatus.VERY_SLOW);
        ACTIVE_THREAD_STATUSES_ORDER.add(ActiveThreadStatus.UNKNOWN);
    }

    private final ActiveTraceLocator activeTraceLocator;
    private final int activeThreadStatusCount;

    public ActiveThreadService(ActiveTraceLocator activeTraceLocator) {
        if (activeTraceLocator == null) {
            throw new NullPointerException("activeTraceLocator");
        }
        this.activeTraceLocator = activeTraceLocator;
        this.activeThreadStatusCount = ACTIVE_THREAD_STATUSES_ORDER.size();
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase tBase) {
        if (tBase == null || !(tBase instanceof TActiveThread)) {
            TResult fail = new TResult();
            fail.setSuccess(false);
            fail.setMessage("Expected object type error. expected:" + getCommandClazz() + ", but was:" + ClassUtils.simpleClassName(tBase));
            return fail;
        }

        Map<ActiveThreadStatus, AtomicInteger> mappedStatus = new LinkedHashMap<ActiveThreadStatus, AtomicInteger>(activeThreadStatusCount);
        for (ActiveThreadStatus status : ACTIVE_THREAD_STATUSES_ORDER) {
            mappedStatus.put(status, new AtomicInteger(0));
        }

        long currentTime = System.currentTimeMillis();

        List<ActiveTraceInfo> activeTraceInfoCollect = activeTraceLocator.collect();
        for (ActiveTraceInfo activeTraceInfo : activeTraceInfoCollect) {
            ActiveThreadStatus status = ActiveThreadStatus.getStatus(currentTime - activeTraceInfo.getStartTime());
            mappedStatus.get(status).incrementAndGet();
        }

        List<Integer> activeThreadCount = new ArrayList<Integer>(activeThreadStatusCount);
        for (AtomicInteger statusCount : mappedStatus.values()) {
            activeThreadCount.add(statusCount.get());
        }

        TActiveThreadResponse response = new TActiveThreadResponse();
        response.setActiveThreadCount(activeThreadCount);

        return response;
    }

    @Override
    public Class<? extends TBase> getCommandClazz() {
        return TActiveThread.class;
    }

    enum ActiveThreadStatus {
        FAST(0, 1000),
        NORMAL(1000, 3000),
        SLOW(3000, 5000),
        VERY_SLOW(5000, Long.MAX_VALUE),
        UNKNOWN(-1, -1);

        private final long from;
        private final long to;

        private ActiveThreadStatus(long from, long to) {
            this.from = from;
            this.to = to;
        }

        private boolean isThisStatus(long durationTime) {
            if (from < durationTime && durationTime <= to) {
                return true;
            }

            return false;
        }

        private static ActiveThreadStatus getStatus(long durationTime) {

            ActiveThreadStatus[] statuses = ActiveThreadStatus.values();
            for (ActiveThreadStatus status : statuses) {
                boolean thisStatus = status.isThisStatus(durationTime);
                if (thisStatus) {
                    return status;
                }
            }

            return UNKNOWN;
        }

    }

}
