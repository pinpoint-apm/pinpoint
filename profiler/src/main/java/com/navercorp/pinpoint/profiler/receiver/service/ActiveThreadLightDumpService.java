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
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceInfo;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import org.apache.thrift.TBase;

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
        boolean enableFilterThreadName = request.getTargetThreadNameListSize() > 0;
        boolean enableFilterTraceId = request.getTraceIdListSize() > 0;

        TCmdActiveThreadLightDumpRes response = new TCmdActiveThreadLightDumpRes();

        long currentTime = System.currentTimeMillis();
        List<ActiveTraceInfo> collectedActiveTraceInfo = activeTraceLocator.collect();
        for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
            long execTime = currentTime - activeTraceInfo.getStartTime();
            if (execTime >= request.getExecTime()) {
                if (!isTraceThread(activeTraceInfo, enableFilterThreadName, request.getTargetThreadNameList(), enableFilterTraceId, request.getTraceIdList())) {
                    continue;
                }

                Thread thread = activeTraceInfo.getThread();
                TThreadLightDump threadDump = new TThreadLightDump();
                threadDump.setThreadName(thread.getName());
                threadDump.setThreadId(thread.getId());
                threadDump.setThreadState(ThreadDumpUtils.toTThreadState(thread.getState()));

                TActiveThreadLightDump activeThreadDump = new TActiveThreadLightDump();
                activeThreadDump.setExecTime(execTime);
                activeThreadDump.setTraceId(activeTraceInfo.getId());
                activeThreadDump.setThreadDump(threadDump);

                response.addToThreadDumps(activeThreadDump);
            }
        }
        response.setJvmType(JvmUtils.getType().name());
        response.setJvmVersion(JvmUtils.getVersion().name());
        return response;
    }

    private boolean isTraceThread(ActiveTraceInfo activeTraceInfo, boolean enableFilterThreadName, List<String> threadNameList, boolean enableFilterTraceId, List<Long> traceIdList) {
        Thread thread = activeTraceInfo.getThread();
        if (thread == null) {
            return false;
        }
        if (!enableFilterThreadName && !enableFilterTraceId) {
            return true;
        }
        if (enableFilterTraceId) {
            long traceId = activeTraceInfo.getId();
            if (traceIdList.contains(traceId)) {
                return true;
            }
        }
        if (enableFilterThreadName) {
            if (threadNameList.contains(thread.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<? extends TBase> getCommandClazz() {
        return TCmdActiveThreadLightDump.class;
    }

}

