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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectJobTest {

    @Test
    public void run() throws Exception {
        AgentStatMetricCollector<AgentStatMetricSnapshot> agentStatMetricCollector = mockAgentStatMetricCollector();
        Mockito.when(agentStatMetricCollector.collect()).thenReturn(new AgentStatMetricSnapshot());

        DataSender dataSender = mock(DataSender.class);

        CollectJob job = new CollectJob(dataSender, "agent", 0, agentStatMetricCollector, 1);
        job.run();

        Mockito.verify(dataSender).send(any(AgentStatMetricSnapshotBatch.class));

    }

    @SuppressWarnings("unchecked")
    private AgentStatMetricCollector<AgentStatMetricSnapshot> mockAgentStatMetricCollector() {
        return Mockito.mock(AgentStatMetricCollector.class);
    }


}