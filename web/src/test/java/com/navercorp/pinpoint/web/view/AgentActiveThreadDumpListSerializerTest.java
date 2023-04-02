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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.context.thrift.ThreadDumpThriftMessageConverter;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TThreadDump;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadDumpFactory;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadDumpList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDumpListSerializerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ThreadDumpThriftMessageConverter threadDumpThriftMessageConverter = new ThreadDumpThriftMessageConverter();

    @Test
    public void serializeTest() throws Exception {
        ThreadInfo[] allThreadInfo = ThreadMXBeanUtils.dumpAllThread();
        AgentActiveThreadDumpList activeThreadDumpList = createThreadDumpList(allThreadInfo);
        String jsonValue = mapper.writeValueAsString(activeThreadDumpList);

        List<Map<String, Object>> list = mapper.readValue(jsonValue, TypeRef.listMap());

        Assertions.assertTrue(CollectionUtils.hasLength(list));

        Map<String, Object> map = list.get(0);

        assertThat(map)
                .containsKey("threadId")
                .containsKey("threadName")
                .containsKey("threadState")
                .containsKey("startTime")
                .containsKey("execTime")
                .containsKey("localTraceId")
                .containsKey("sampled")
                .containsKey("transactionId")
                .containsKey("entryPoint")
                .containsKey("detailMessage");
    }

    private AgentActiveThreadDumpList createThreadDumpList(ThreadInfo[] allThreadInfo) {
        List<TActiveThreadDump> activeThreadDumpList = new ArrayList<>();
        for (ThreadInfo threadInfo : allThreadInfo) {
            TActiveThreadDump tActiveThreadDump = new TActiveThreadDump();
            tActiveThreadDump.setStartTime(System.currentTimeMillis() - 1000);

            final ThreadDumpMetricSnapshot threadDumpMetricSnapshot = ThreadDumpUtils.createThreadDump(threadInfo);
            final TThreadDump threadDump = this.threadDumpThriftMessageConverter.toMessage(threadDumpMetricSnapshot);
            tActiveThreadDump.setThreadDump(threadDump);
            activeThreadDumpList.add(tActiveThreadDump);
        }

        AgentActiveThreadDumpFactory factory = new AgentActiveThreadDumpFactory();
        return factory.create1(activeThreadDumpList);
    }

}
