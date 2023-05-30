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

package com.navercorp.pinpoint.web.vo.activethread;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.grpc.trace.PThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PThreadState;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import com.navercorp.pinpoint.web.util.ThreadDumpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDumpFactory {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public AgentActiveThreadDumpFactory() {
    }

    public AgentActiveThreadDumpList create1(List<PActiveThreadDump> tActiveThreadDumpList) {
        if (CollectionUtils.isEmpty(tActiveThreadDumpList)) {
            return AgentActiveThreadDumpList.EMPTY_INSTANCE;
        }

        AgentActiveThreadDumpList result = new AgentActiveThreadDumpList(tActiveThreadDumpList.size());
        for (PActiveThreadDump activeThreadDump : tActiveThreadDumpList) {
            try {
                AgentActiveThreadDump agentActiveThreadDump = create1(activeThreadDump);
                result.add(agentActiveThreadDump);
            } catch (Exception e) {
                logger.warn("create AgentActiveThreadDump fail. arguments(TActiveThreadDump:{})", activeThreadDump);
            }
        }
        return result;
    }

    private AgentActiveThreadDump create1(PActiveThreadDump tActiveThreadDump) {
        Objects.requireNonNull(tActiveThreadDump, "tActiveThreadDump");


        PThreadDump activeThreadDump = tActiveThreadDump.getThreadDump();

        AgentActiveThreadDump.Builder builder = new AgentActiveThreadDump.Builder();
        builder.setThreadId(activeThreadDump.getThreadId());
        builder.setThreadName(activeThreadDump.getThreadName());
        builder.setThreadState(getThreadState(activeThreadDump.getThreadState()));

        builder.setStartTime(tActiveThreadDump.getStartTime());
        builder.setExecTime(System.currentTimeMillis() - tActiveThreadDump.getStartTime());
        builder.setLocalTraceId(tActiveThreadDump.getLocalTraceId());

        builder.setSampled(tActiveThreadDump.getSampled());
        builder.setTransactionId(tActiveThreadDump.getTransactionId());
        builder.setEntryPoint(tActiveThreadDump.getEntryPoint());

        builder.setDetailMessage(ThreadDumpUtils.createDumpMessage(activeThreadDump));

        return builder.build();
    }

    public AgentActiveThreadDumpList create2(List<PActiveThreadLightDump> tActiveThreadLightDumpList) {
        if (CollectionUtils.isEmpty(tActiveThreadLightDumpList)) {
            return AgentActiveThreadDumpList.EMPTY_INSTANCE;
        }

        AgentActiveThreadDumpList result = new AgentActiveThreadDumpList(tActiveThreadLightDumpList.size());
        for (PActiveThreadLightDump activeThreadLightDump : tActiveThreadLightDumpList) {
            try {
                AgentActiveThreadDump agentActiveThreadDump = create2(activeThreadLightDump);
                result.add(agentActiveThreadDump);
            } catch (Exception e) {
                logger.warn("create AgentActiveThreadDump fail. arguments(TActiveThreadDump:{})", activeThreadLightDump);
            }
        }
        return result;
    }

    private AgentActiveThreadDump create2(PActiveThreadLightDump tActiveThreadLightDump) {
        Objects.requireNonNull(tActiveThreadLightDump, "tActiveThreadLightDump");


        PThreadLightDump activeThreadDump = tActiveThreadLightDump.getThreadDump();
        AgentActiveThreadDump.Builder builder = new AgentActiveThreadDump.Builder();
        builder.setThreadId(activeThreadDump.getThreadId());
        builder.setThreadName(activeThreadDump.getThreadName());
        builder.setThreadState(getThreadState(activeThreadDump.getThreadState()));

        builder.setStartTime(tActiveThreadLightDump.getStartTime());
        builder.setExecTime(System.currentTimeMillis() - tActiveThreadLightDump.getStartTime());
        builder.setLocalTraceId(tActiveThreadLightDump.getLocalTraceId());

        builder.setSampled(tActiveThreadLightDump.getSampled());
        builder.setTransactionId(tActiveThreadLightDump.getTransactionId());
        builder.setEntryPoint(tActiveThreadLightDump.getEntryPoint());

        builder.setDetailMessage(StringUtils.EMPTY);

        return builder.build();
    }

    private TThreadState getThreadState(PThreadState threadState) {
        if (threadState == null) {
            return TThreadState.UNKNOWN;
        } else {
            return TThreadState.findByValue(threadState.getNumber());
        }
    }

}
