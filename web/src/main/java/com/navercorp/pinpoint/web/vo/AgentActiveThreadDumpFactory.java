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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadState;
import com.navercorp.pinpoint.web.util.ThreadDumpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDumpFactory {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String TAB_SEPARATOR = "    "; // tab to 4 spaces

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public AgentActiveThreadDumpFactory() {
    }

    public AgentActiveThreadDumpList create1(List<TActiveThreadDump> tActiveThreadDumpList) {
        if (CollectionUtils.isEmpty(tActiveThreadDumpList)) {
            return AgentActiveThreadDumpList.EMPTY_INSTANCE;
        }

        AgentActiveThreadDumpList result = new AgentActiveThreadDumpList(tActiveThreadDumpList.size());
        for (TActiveThreadDump activeThreadDump : tActiveThreadDumpList) {
            try {
                AgentActiveThreadDump agentActiveThreadDump = create1(activeThreadDump);
                result.add(agentActiveThreadDump);
            } catch (Exception e) {
                logger.warn("create AgentActiveThreadDump fail. arguments(TActiveThreadDump:{})", activeThreadDump);
            }
        }
        return result;
    }

    private AgentActiveThreadDump create1(TActiveThreadDump tActiveThreadDump) {
        if (tActiveThreadDump == null) {
            throw new NullPointerException("tActiveThreadDump");
        }

        TThreadDump activeThreadDump = tActiveThreadDump.getThreadDump();

        AgentActiveThreadDump.Builder builder = new AgentActiveThreadDump.Builder();
        builder.setThreadId(activeThreadDump.getThreadId());
        builder.setThreadName(activeThreadDump.getThreadName());
        builder.setThreadState(getThreadState(activeThreadDump.getThreadState()));

        builder.setStartTime(tActiveThreadDump.getStartTime());
        builder.setExecTime(System.currentTimeMillis() - tActiveThreadDump.getStartTime());
        builder.setLocalTraceId(tActiveThreadDump.getLocalTraceId());

        builder.setSampled(tActiveThreadDump.isSampled());
        builder.setTransactionId(tActiveThreadDump.getTransactionId());
        builder.setEntryPoint(tActiveThreadDump.getEntryPoint());

        builder.setDetailMessage(ThreadDumpUtils.createDumpMessage(activeThreadDump));

        return builder.build();
    }

    public AgentActiveThreadDumpList create2(List<TActiveThreadLightDump> tActiveThreadLightDumpList) {
        if (CollectionUtils.isEmpty(tActiveThreadLightDumpList)) {
            return AgentActiveThreadDumpList.EMPTY_INSTANCE;
        }

        AgentActiveThreadDumpList result = new AgentActiveThreadDumpList(tActiveThreadLightDumpList.size());
        for (TActiveThreadLightDump activeThreadLightDump : tActiveThreadLightDumpList) {
            try {
                AgentActiveThreadDump agentActiveThreadDump = create2(activeThreadLightDump);
                result.add(agentActiveThreadDump);
            } catch (Exception e) {
                logger.warn("create AgentActiveThreadDump fail. arguments(TActiveThreadDump:{})", activeThreadLightDump);
            }
        }
        return result;
    }

    private AgentActiveThreadDump create2(TActiveThreadLightDump tActiveThreadLightDump) {
        if (tActiveThreadLightDump == null) {
            throw new NullPointerException("tActiveThreadLightDump");
        }

        TThreadLightDump activeThreadDump = tActiveThreadLightDump.getThreadDump();
        AgentActiveThreadDump.Builder builder = new AgentActiveThreadDump.Builder();
        builder.setThreadId(activeThreadDump.getThreadId());
        builder.setThreadName(activeThreadDump.getThreadName());
        builder.setThreadState(getThreadState(activeThreadDump.getThreadState()));

        builder.setStartTime(tActiveThreadLightDump.getStartTime());
        builder.setExecTime(System.currentTimeMillis() - tActiveThreadLightDump.getStartTime());
        builder.setLocalTraceId(tActiveThreadLightDump.getLocalTraceId());

        builder.setSampled(tActiveThreadLightDump.isSampled());
        builder.setTransactionId(tActiveThreadLightDump.getTransactionId());
        builder.setEntryPoint(tActiveThreadLightDump.getEntryPoint());

        builder.setDetailMessage(StringUtils.EMPTY);

        return builder.build();
    }

    private TThreadState getThreadState(TThreadState threadState) {
        if (threadState == null) {
            return TThreadState.UNKNOWN;
        } else {
            return threadState;
        }
    }

}
