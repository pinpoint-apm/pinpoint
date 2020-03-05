/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.thrift.ThreadStateThriftMessageConverter;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.TBase;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ActiveThreadLightDumpService implements ProfilerRequestCommandService<TBase<?, ?>, TBase<?, ?>> {

    private final ActiveThreadDumpCoreService activeThreadDump;
    private final ThreadStateThriftMessageConverter threadStateThriftMessageConverter = new ThreadStateThriftMessageConverter();

    public ActiveThreadLightDumpService(ActiveThreadDumpCoreService activeThreadDump) {
        this.activeThreadDump = Assert.requireNonNull(activeThreadDump, "activeThreadDump");
    }

    @Override
    public TBase<?, ?> requestCommandService(TBase tBase) {
        TCmdActiveThreadLightDump request = (TCmdActiveThreadLightDump) tBase;

        List<TActiveThreadLightDump> activeThreadDumpList = getActiveThreadDumpList(request);

        TCmdActiveThreadLightDumpRes response = new TCmdActiveThreadLightDumpRes();
        response.setType(ActiveThreadDumpService.JAVA);
        response.setSubType(JvmUtils.getType().name());
        response.setVersion(JvmUtils.getVersion().name());
        response.setThreadDumps(activeThreadDumpList);
        return response;
    }

    private List<TActiveThreadLightDump> getActiveThreadDumpList(TCmdActiveThreadLightDump tRequest) {
        ThreadDumpRequest request = ThreadDumpRequest.create(tRequest);

        Collection<ThreadDump> activeTraceInfoList = activeThreadDump.getActiveThreadDumpList(request);

        return toTActiveThreadLightDump(activeTraceInfoList);
    }



    private List<TActiveThreadLightDump> toTActiveThreadLightDump(Collection<ThreadDump> activeTraceInfoList) {

        final List<TActiveThreadLightDump> result = new ArrayList<TActiveThreadLightDump>(activeTraceInfoList.size());

        for (ThreadDump threadDump : activeTraceInfoList) {
            TActiveThreadLightDump tActiveThreadDump = createActiveThreadDump(threadDump);
            result.add(tActiveThreadDump);
        }

        return result;
    }


    private TThreadLightDump createTThreadLightDump(ThreadInfo threadInfo) {
        TThreadLightDump threadDump = new TThreadLightDump();
        threadDump.setThreadName(threadInfo.getThreadName());
        threadDump.setThreadId(threadInfo.getThreadId());

        final TThreadState threadState = this.threadStateThriftMessageConverter.toMessage(threadInfo.getThreadState());
        threadDump.setThreadState(threadState);
        return threadDump;
    }

    private TActiveThreadLightDump createActiveThreadDump(ThreadDump threadDump) {

        final ActiveTraceSnapshot activeTraceInfo = threadDump.getActiveTraceSnapshot();
        final ThreadInfo threadInfo = threadDump.getThreadInfo();

        TThreadLightDump tThreadLightDump = createTThreadLightDump(threadInfo);

        TActiveThreadLightDump activeThreadDump = new TActiveThreadLightDump();
        activeThreadDump.setStartTime(activeTraceInfo.getStartTime());
        activeThreadDump.setLocalTraceId(activeTraceInfo.getLocalTransactionId());
        activeThreadDump.setThreadDump(tThreadLightDump);

        if (activeTraceInfo.isSampled()) {
            activeThreadDump.setSampled(true);
            activeThreadDump.setTransactionId(activeTraceInfo.getTransactionId());
            activeThreadDump.setEntryPoint(activeTraceInfo.getEntryPoint());
        }
        return activeThreadDump;
    }

    @Override
    public short getCommandServiceCode() {
        return TCommandType.ACTIVE_THREAD_LIGHT_DUMP.getCode();
    }

}
