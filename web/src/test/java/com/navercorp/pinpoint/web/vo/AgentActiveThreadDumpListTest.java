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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.util.ThreadDumpUtils;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ThreadInfo;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadDumpListTest {

    @Test
    public void addAndGetTest() throws Exception {
        ThreadInfo[] allThreadInfo = ThreadMXBeanUtils.dumpAllThread();
        AgentActiveThreadDumpList activeThreadDumpList = createThreadDumpList(allThreadInfo);

        Assert.assertEquals(allThreadInfo.length, activeThreadDumpList.getAgentActiveThreadDumpRepository().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void checkunmodifiableList() throws Exception {
        ThreadInfo[] allThreadInfo = ThreadMXBeanUtils.dumpAllThread();
        AgentActiveThreadDumpList activeThreadDumpList = createThreadDumpList(allThreadInfo);

        List<AgentActiveThreadDump> agentActiveThreadDumpRepository = activeThreadDumpList.getAgentActiveThreadDumpRepository();
        agentActiveThreadDumpRepository.remove(0);
    }

    private AgentActiveThreadDumpList createThreadDumpList(ThreadInfo[] allThreadInfo) {
        AgentActiveThreadDumpList activeThreadDumpList = new AgentActiveThreadDumpList();
        for (ThreadInfo threadInfo : allThreadInfo) {
            TActiveThreadDump tActiveThreadDump = new TActiveThreadDump();
            tActiveThreadDump.setExecTime(1000);
            tActiveThreadDump.setThreadDump(ThreadDumpUtils.createTThreadDump(threadInfo));

            activeThreadDumpList.add(new AgentActiveThreadDump(tActiveThreadDump));
        }
        return activeThreadDumpList;
    }

}
