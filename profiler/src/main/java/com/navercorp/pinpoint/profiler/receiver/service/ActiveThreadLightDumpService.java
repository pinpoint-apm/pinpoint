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

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.profiler.util.ActiveThreadDumpUtils;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import org.apache.thrift.TBase;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ActiveThreadLightDumpService implements ProfilerRequestCommandService {

    private final ActiveTraceRepository activeTraceRepository;

    public ActiveThreadLightDumpService(ActiveTraceRepository activeTraceRepository) {
        this.activeTraceRepository = activeTraceRepository;
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
        List<ActiveTraceSnapshot> activeTraceInfoList = activeTraceRepository.collect();

        int limit = request.getLimit();
        if (limit > 0) {
            Collections.sort(activeTraceInfoList, ActiveThreadDumpUtils.getActiveTraceInfoComparator());
        } else {
            limit = Integer.MAX_VALUE;
        }

        return getTActiveThreadDumpList(request, limit, activeTraceInfoList);
    }

    private List<TActiveThreadLightDump> getTActiveThreadDumpList(TCmdActiveThreadLightDump request, int limit, List<ActiveTraceSnapshot> activeTraceInfoList) {
        int targetThreadNameListSize = request.getThreadNameListSize();
        int localTraceIdListSize = request.getLocalTraceIdListSize();
        boolean filterEnable = (targetThreadNameListSize + localTraceIdListSize) > 0;

        List<TActiveThreadLightDump> activeThreadDumpList = new ArrayList<TActiveThreadLightDump>(Math.min(limit, activeTraceInfoList.size()));
        if (filterEnable) {
            final List<String> threadNameList = request.getThreadNameList();
            for (ActiveTraceSnapshot activeTraceInfo : activeTraceInfoList) {
                if (!ActiveThreadDumpUtils.isTraceThread(activeTraceInfo, threadNameList, request.getLocalTraceIdList())) {
                    continue;
                }

                TActiveThreadLightDump activeThreadDump = createActiveLightThreadDump(activeTraceInfo);
                if (activeThreadDump != null) {
                    if (limit > activeThreadDumpList.size()) {
                        activeThreadDumpList.add(activeThreadDump);
                    }
                }
            }
        } else {
            for (ActiveTraceSnapshot activeTraceInfo : activeTraceInfoList) {
                TActiveThreadLightDump activeThreadDump = createActiveLightThreadDump(activeTraceInfo);
                if (activeThreadDump != null) {
                    if (limit > activeThreadDumpList.size()) {
                        activeThreadDumpList.add(activeThreadDump);
                    }
                }
            }
        }

        return activeThreadDumpList;
    }

    private TActiveThreadLightDump createActiveLightThreadDump(ActiveTraceSnapshot activeTraceInfo) {
        final long threadId = activeTraceInfo.getThreadId();
        if (threadId == -1) {
            return null;
        }
        ThreadInfo threadInfo = ThreadMXBeanUtils.findThread(threadId, 0);
        TThreadLightDump threadDump = createTThreadLightDump(threadInfo);
        return createActiveThreadDump(activeTraceInfo, threadDump);
    }


    private TThreadLightDump createTThreadLightDump(ThreadInfo threadInfo) {
        TThreadLightDump threadDump = new TThreadLightDump();
        threadDump.setThreadName(threadInfo.getThreadName());
        threadDump.setThreadId(threadInfo.getThreadId());

        threadDump.setThreadState(ThreadDumpUtils.toTThreadState(threadInfo.getThreadState()));
        return threadDump;
    }

    private TActiveThreadLightDump createActiveThreadDump(ActiveTraceSnapshot activeTraceInfo, TThreadLightDump threadDump) {
        TActiveThreadLightDump activeThreadDump = new TActiveThreadLightDump();
        activeThreadDump.setStartTime(activeTraceInfo.getStartTime());
        activeThreadDump.setLocalTraceId(activeTraceInfo.getLocalTransactionId());
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
