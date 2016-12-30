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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.util.FixedMaxSizeTreeSet;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceInfo;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.profiler.util.ActiveThreadDumpUtils;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ActiveThreadLightDumpService implements ProfilerRequestCommandService {

    private final ActiveTraceLocator activeTraceLocator;

    public ActiveThreadLightDumpService(ActiveTraceLocator activeTraceLocator) {
        this.activeTraceLocator = activeTraceLocator;
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase tBase) {
        TCmdActiveThreadLightDump request = (TCmdActiveThreadLightDump) tBase;

        List<TActiveThreadLightDump> activeThreadDumpList = getActiveThreadDumpList(request);

        TCmdActiveThreadLightDumpRes response = new TCmdActiveThreadLightDumpRes();
        response.setType("JAVA");
        response.setSubType(JvmUtils.getType().name());
        response.setVersion(JvmUtils.getVersion().name());
        response.setThreadDumps(activeThreadDumpList);
        return response;
    }

    private List<TActiveThreadLightDump> getActiveThreadDumpList(TCmdActiveThreadLightDump request) {
        List<ActiveTraceInfo> activeTraceInfoList = activeTraceLocator.collect();

        int limit = request.getLimit();
        if (limit > 0 && activeTraceInfoList.size() > limit) {
            return getFixedSizeActiveThreadDumpList(request, limit, activeTraceInfoList);
        } else {
            return getTActiveThreadDumpList(request, activeTraceInfoList);
        }
    }

    private List<TActiveThreadLightDump> getFixedSizeActiveThreadDumpList(TCmdActiveThreadLightDump request, int capacity, List<ActiveTraceInfo> collectedActiveTraceInfo) {
        int targetThreadNameListSize = request.getTargetThreadNameListSize();
        int localTraceIdListSize = request.getLocalTraceIdListSize();

        boolean filterEnable = (targetThreadNameListSize + localTraceIdListSize) > 0;

        FixedMaxSizeTreeSet<TActiveThreadLightDump> fixedSizeActiveThreadDumpSet = new FixedMaxSizeTreeSet<TActiveThreadLightDump>(capacity, ActiveThreadDumpUtils.getLightDumpComparator());
        if (filterEnable) {
            for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
                if (!ActiveThreadDumpUtils.isTraceThread(activeTraceInfo, request.getTargetThreadNameList(), request.getLocalTraceIdList())) {
                    continue;
                }

                TActiveThreadLightDump activeThreadDump = createActiveLightThreadDump(activeTraceInfo);
                if (activeThreadDump != null) {
                    fixedSizeActiveThreadDumpSet.add(activeThreadDump);
                }
            }
        } else {
            for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
                TActiveThreadLightDump activeThreadDump = createActiveLightThreadDump(activeTraceInfo);
                if (activeThreadDump != null) {
                    fixedSizeActiveThreadDumpSet.add(activeThreadDump);
                }
            }
        }

        return fixedSizeActiveThreadDumpSet.getList();
    }

    private List<TActiveThreadLightDump> getTActiveThreadDumpList(TCmdActiveThreadLightDump request, List<ActiveTraceInfo> collectedActiveTraceInfo) {
        int targetThreadNameListSize = request.getTargetThreadNameListSize();
        int localTraceIdListSize = request.getLocalTraceIdListSize();

        boolean filterEnable = (targetThreadNameListSize + localTraceIdListSize) > 0;

        List<TActiveThreadLightDump> activeThreadDumpList = new ArrayList<TActiveThreadLightDump>(collectedActiveTraceInfo.size());

        if (filterEnable) {
            for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
                if (!ActiveThreadDumpUtils.isTraceThread(activeTraceInfo, request.getTargetThreadNameList(), request.getLocalTraceIdList())) {
                    continue;
                }

                TActiveThreadLightDump activeThreadDump1 = createActiveLightThreadDump(activeTraceInfo);
                ListUtils.addIfValueNotNull(activeThreadDumpList, activeThreadDump1);
            }
        } else {
            for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
                TActiveThreadLightDump activeThreadDump1 = createActiveLightThreadDump(activeTraceInfo);
                ListUtils.addIfValueNotNull(activeThreadDumpList, activeThreadDump1);
            }
        }

        return activeThreadDumpList;
    }

    private TActiveThreadLightDump createActiveLightThreadDump(ActiveTraceInfo activeTraceInfo) {
        Thread thread = activeTraceInfo.getThread();
        if (thread == null) {
            return null;
        }
        TThreadLightDump threadDump = createThreadDump(thread);
        return createActiveThreadDump(activeTraceInfo, threadDump);
    }


    private TThreadLightDump createThreadDump(Thread thread) {
        TThreadLightDump threadDump = new TThreadLightDump();
        threadDump.setThreadName(thread.getName());
        threadDump.setThreadId(thread.getId());
        threadDump.setThreadState(ThreadDumpUtils.toTThreadState(thread.getState()));
        return threadDump;
    }

    private TActiveThreadLightDump createActiveThreadDump(ActiveTraceInfo activeTraceInfo, TThreadLightDump threadDump) {
        TActiveThreadLightDump activeThreadDump = new TActiveThreadLightDump();
        activeThreadDump.setStartTime(activeTraceInfo.getStartTime());
        activeThreadDump.setLocalTraceId(activeTraceInfo.getLocalTraceId());
        activeThreadDump.setThreadDump(threadDump);

        if (activeTraceInfo.isSampled()) {
            activeThreadDump.setSampled(true);
            activeThreadDump.setTransactionId(activeTraceInfo.getTransactionId());
            activeThreadDump.setEntryPoint(activeTraceInfo.getEntryPoint());
        }
        return activeThreadDump;
    }

    @Override
    public Class<? extends TBase> getCommandClazz() {
        return TCmdActiveThreadLightDump.class;
    }

}

