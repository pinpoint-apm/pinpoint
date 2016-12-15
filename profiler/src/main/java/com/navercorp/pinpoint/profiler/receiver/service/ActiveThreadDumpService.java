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

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceInfo;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import org.apache.thrift.TBase;

import java.util.List;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadDumpService implements ProfilerRequestCommandService {

    private final ActiveTraceLocator activeTraceLocator;

    public ActiveThreadDumpService(ActiveTraceLocator activeTraceLocator) {
        this.activeTraceLocator = activeTraceLocator;
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase tBase) {
        TCmdActiveThreadDump request = (TCmdActiveThreadDump) tBase;
        boolean enableFilterThreadName = request.getTargetThreadNameListSize() > 0;

        TCmdActiveThreadDumpRes response = new TCmdActiveThreadDumpRes();

        long currentTime = System.currentTimeMillis();
        List<ActiveTraceInfo> collectedActiveTraceInfo = activeTraceLocator.collect();
        for (ActiveTraceInfo activeTraceInfo : collectedActiveTraceInfo) {
            long execTime = currentTime - activeTraceInfo.getStartTime();
            if (execTime >= request.getExecTime()) {
                Thread thread = activeTraceInfo.getThread();
                if (thread == null) {
                    continue;
                }
                if (enableFilterThreadName) {
                    if (!request.getTargetThreadNameList().contains(thread.getName())) {
                        continue;
                    }
                }

                TThreadDump threadDump = createThreadDump(thread, true);
                if (threadDump != null) {
                    TActiveThreadDump activeThreadDump = new TActiveThreadDump();
                    activeThreadDump.setExecTime(execTime);
                    activeThreadDump.setThreadDump(threadDump);

                    response.addToThreadDumps(activeThreadDump);
                }
            }
        }
        response.setJvmType(JvmUtils.getType().name());
        response.setJvmVersion(JvmUtils.getVersion().name());
        return response;
    }

    private TThreadDump createThreadDump(Thread thread, boolean isIncludeStackTrace) {
        if (thread == null) {
            return null;
        }

        if (isIncludeStackTrace) {
            return ThreadDumpUtils.createTThreadDump(thread);
        } else {
            return ThreadDumpUtils.createTThreadDump(thread, 0);
        }
    }

    @Override
    public Class<? extends TBase> getCommandClazz() {
        return TCmdActiveThreadDump.class;
    }

}
