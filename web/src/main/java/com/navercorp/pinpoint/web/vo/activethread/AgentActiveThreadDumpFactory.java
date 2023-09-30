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

import com.navercorp.pinpoint.common.server.bo.event.ThreadDumpBo;
import com.navercorp.pinpoint.common.server.bo.event.ThreadState;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.realtime.dto.ActiveThreadDump;
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

    public AgentActiveThreadDumpList create1(List<ActiveThreadDump> activeThreadDumps) {
        if (CollectionUtils.isEmpty(activeThreadDumps)) {
            return AgentActiveThreadDumpList.EMPTY_INSTANCE;
        }

        AgentActiveThreadDumpList result = new AgentActiveThreadDumpList(activeThreadDumps.size());
        for (ActiveThreadDump activeThreadDump : activeThreadDumps) {
            try {
                AgentActiveThreadDump agentActiveThreadDump = create1(activeThreadDump);
                result.add(agentActiveThreadDump);
            } catch (Exception e) {
                logger.warn("create AgentActiveThreadDump fail. arguments(TActiveThreadDump:{})", activeThreadDump);
            }
        }
        return result;
    }

    private AgentActiveThreadDump create1(ActiveThreadDump activeThreadDump) {
        Objects.requireNonNull(activeThreadDump, "activeThreadDump");


        ThreadDumpBo threadDump = Objects.requireNonNull(activeThreadDump.getThreadDump(), "threadDump");

        AgentActiveThreadDump.Builder builder = new AgentActiveThreadDump.Builder();
        builder.setThreadId(threadDump.getThreadId());
        builder.setThreadName(threadDump.getThreadName());
        builder.setThreadState(getThreadState(threadDump.getThreadState()));

        builder.setStartTime(activeThreadDump.getStartTime());
        builder.setExecTime(System.currentTimeMillis() - activeThreadDump.getStartTime());
        builder.setLocalTraceId(activeThreadDump.getLocalTraceId());

        builder.setSampled(activeThreadDump.isSampled());
        builder.setTransactionId(activeThreadDump.getTransactionId());
        builder.setEntryPoint(activeThreadDump.getEntryPoint());

        builder.setDetailMessage(ThreadDumpUtils.createDumpMessage(threadDump));

        return builder.build();
    }

    public AgentActiveThreadDumpList create2(List<ActiveThreadDump> tActiveThreadLightDumpList) {
        if (CollectionUtils.isEmpty(tActiveThreadLightDumpList)) {
            return AgentActiveThreadDumpList.EMPTY_INSTANCE;
        }

        AgentActiveThreadDumpList result = new AgentActiveThreadDumpList(tActiveThreadLightDumpList.size());
        for (ActiveThreadDump activeThreadLightDump : tActiveThreadLightDumpList) {
            try {
                AgentActiveThreadDump agentActiveThreadDump = create2(activeThreadLightDump);
                result.add(agentActiveThreadDump);
            } catch (Exception e) {
                logger.warn("create AgentActiveThreadDump fail. arguments(TActiveThreadDump:{})", activeThreadLightDump);
            }
        }
        return result;
    }

    private AgentActiveThreadDump create2(ActiveThreadDump activeThreadDump) {
        Objects.requireNonNull(activeThreadDump, "activeThreadDump");


        ThreadDumpBo threadDump = Objects.requireNonNull(activeThreadDump.getThreadDump(), "threadDump");
        AgentActiveThreadDump.Builder builder = new AgentActiveThreadDump.Builder();
        builder.setThreadId(threadDump.getThreadId());
        builder.setThreadName(threadDump.getThreadName());
        builder.setThreadState(getThreadState(threadDump.getThreadState()));

        builder.setStartTime(activeThreadDump.getStartTime());
        builder.setExecTime(System.currentTimeMillis() - activeThreadDump.getStartTime());
        builder.setLocalTraceId(activeThreadDump.getLocalTraceId());

        builder.setSampled(activeThreadDump.isSampled());
        builder.setTransactionId(activeThreadDump.getTransactionId());
        builder.setEntryPoint(activeThreadDump.getEntryPoint());

        builder.setDetailMessage(StringUtils.EMPTY);

        return builder.build();
    }

    private TThreadState getThreadState(ThreadState threadState) {
        if (threadState == null) {
            return TThreadState.UNKNOWN;
        } else {
            return TThreadState.findByValue(threadState.getValue());
        }
    }

}
