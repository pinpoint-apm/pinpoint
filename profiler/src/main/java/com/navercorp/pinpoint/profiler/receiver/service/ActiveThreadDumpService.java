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

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.thrift.ThreadDumpThriftMessageConverter;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.profiler.receiver.ProfilerRequestCommandService;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.TBase;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ActiveThreadDumpService implements ProfilerRequestCommandService<TBase<?, ?>, TBase<?, ?>> {

    static final String JAVA = "JAVA";

    private final ActiveThreadDumpCoreService activeThreadDumpCoreService;
    private final ThreadDumpThriftMessageConverter threadDumpThriftMessageConverter = new ThreadDumpThriftMessageConverter();

    public ActiveThreadDumpService(ActiveThreadDumpCoreService activeThreadDumpCoreService) {
        this.activeThreadDumpCoreService = activeThreadDumpCoreService;
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

    private List<TActiveThreadDump> getActiveThreadDumpList(TCmdActiveThreadDump tRequest) {

        final ThreadDumpRequest request = ThreadDumpRequest.create(tRequest);

        Collection<ThreadDump> activeThreadDumpList = activeThreadDumpCoreService.getActiveThreadDumpList(request);

        return toTActiveThreadDump(activeThreadDumpList);
    }


    private List<TActiveThreadDump> toTActiveThreadDump(Collection<ThreadDump> activeTraceInfoList) {

        final List<TActiveThreadDump> result = new ArrayList<TActiveThreadDump>(activeTraceInfoList.size());
        for (ThreadDump threadDump : activeTraceInfoList) {
            TActiveThreadDump tActiveThreadDump = createTActiveThreadDump(threadDump);
            result.add(tActiveThreadDump);
        }

        return result;
    }


    private TActiveThreadDump createTActiveThreadDump(ThreadDump threadDump) {
        final ActiveTraceSnapshot activeTraceInfo = threadDump.getActiveTraceSnapshot();
        final ThreadInfo threadInfo = threadDump.getThreadInfo();

        final ThreadDumpMetricSnapshot threadDumpMetricSnapshot = ThreadDumpUtils.createThreadDump(threadInfo);
        final TThreadDump tThreadDump = this.threadDumpThriftMessageConverter.toMessage(threadDumpMetricSnapshot);

        TActiveThreadDump activeThreadDump = new TActiveThreadDump();
        activeThreadDump.setStartTime(activeTraceInfo.getStartTime());
        activeThreadDump.setLocalTraceId(activeTraceInfo.getLocalTransactionId());
        activeThreadDump.setThreadDump(tThreadDump);

        if (activeTraceInfo.isSampled()) {
            activeThreadDump.setSampled(true);
            activeThreadDump.setTransactionId(activeTraceInfo.getTransactionId());
            activeThreadDump.setEntryPoint(activeTraceInfo.getEntryPoint());
        }
        return activeThreadDump;
    }

    @Override
    public short getCommandServiceCode() {
        return TCommandType.ACTIVE_THREAD_DUMP.getCode();
    }

}
