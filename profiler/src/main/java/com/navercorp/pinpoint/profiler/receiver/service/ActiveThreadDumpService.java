/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.profiler.util.ActiveThreadDumpUtils;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ActiveThreadDumpService implements ProfilerRequestCommandService {

    private static final String JAVA = "JAVA";
    // TODO extract config
    // See DefaultActiveTraceRepository.DEFAULT_MAX_ACTIVE_TRACE_SIZE = 1024 * 10
    static final int MAX_THREAD_DUMP_LIMIT = 1024 * 2;

    private final ActiveTraceRepository activeTraceRepository;

    public ActiveThreadDumpService(ActiveTraceRepository activeTraceRepository) {
        this.activeTraceRepository = activeTraceRepository;
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase tBase) {
        TCmdActiveThreadDump request = (TCmdActiveThreadDump) tBase;

        List<TActiveThreadDump> activeThreadDumpList = getActiveThreadDumpList(request);

        TCmdActiveThreadDumpRes response = new TCmdActiveThreadDumpRes();
        response.setType(JAVA);
        response.setSubType(JvmUtils.getType().name());
        response.setVersion(JvmUtils.getVersion().name());
        response.setThreadDumps(activeThreadDumpList);
        return response;
    }

    private List<TActiveThreadDump> getActiveThreadDumpList(TCmdActiveThreadDump request) {
        List<ActiveTraceSnapshot> activeTraceInfoList = activeTraceRepository.collect();

        final int limit = getLimit(request.getLimit());
        if (activeTraceInfoList.size() > limit) {
            Collections.sort(activeTraceInfoList, ActiveThreadDumpUtils.getActiveTraceInfoComparator());
        }

        return getActiveThreadDumpList(request, limit, activeTraceInfoList);
    }

    @VisibleForTesting
    static int getLimit(int limit) {
        if (0 >= limit) {
            return MAX_THREAD_DUMP_LIMIT;
        }
        return Math.min(limit, MAX_THREAD_DUMP_LIMIT);
    }

    private List<TActiveThreadDump> getActiveThreadDumpList(TCmdActiveThreadDump request, int limit, List<ActiveTraceSnapshot> activeTraceInfoList) {
        int targetThreadNameListSize = request.getThreadNameListSize();
        int localTraceIdListSize = request.getLocalTraceIdListSize();
        boolean filterEnable = (targetThreadNameListSize + localTraceIdListSize) > 0;

        final int arrayLength = Math.min(limit, activeTraceInfoList.size());
        List<TActiveThreadDump> activeThreadDumpList = new ArrayList<TActiveThreadDump>(arrayLength);
        if (filterEnable) {
            for (ActiveTraceSnapshot activeTraceInfo : activeTraceInfoList) {
                if (!ActiveThreadDumpUtils.isTraceThread(activeTraceInfo, request.getThreadNameList(), request.getLocalTraceIdList())) {
                    continue;
                }

                TActiveThreadDump activeThreadDump = createActiveThreadDump(activeTraceInfo);
                if (activeThreadDump != null) {
                    if (limit > activeThreadDumpList.size()) {
                        activeThreadDumpList.add(activeThreadDump);
                    }
                }
            }
        } else {
            for (ActiveTraceSnapshot activeTraceInfo : activeTraceInfoList) {
                TActiveThreadDump activeThreadDump = createActiveThreadDump(activeTraceInfo);
                if (activeThreadDump != null) {
                    if (limit > activeThreadDumpList.size()) {
                        activeThreadDumpList.add(activeThreadDump);
                    }
                }
            }
        }

        return activeThreadDumpList;
    }

    private TActiveThreadDump createActiveThreadDump(ActiveTraceSnapshot activeTraceInfo) {
        final long threadId = activeTraceInfo.getThreadId();
        TThreadDump threadDump = createThreadDump(threadId, true);
        if (threadDump != null) {
            return createTActiveThreadDump(activeTraceInfo, threadDump);
        }
        return null;
    }

    private TThreadDump createThreadDump(long threadId, boolean isIncludeStackTrace) {
        if (threadId == -1) {
            return null;
        }

        if (isIncludeStackTrace) {
            return ThreadDumpUtils.createTThreadDump(threadId);
        } else {
            return ThreadDumpUtils.createTThreadDump(threadId, 0);
        }
    }

    private TActiveThreadDump createTActiveThreadDump(ActiveTraceSnapshot activeTraceInfo, TThreadDump threadDump) {
        TActiveThreadDump activeThreadDump = new TActiveThreadDump();
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
        return TCmdActiveThreadDump.class;
    }

}
