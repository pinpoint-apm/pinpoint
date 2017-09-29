/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.DefaultAgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.util.TestInterceptorRegistryBinder;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.instrument.Instrumentation;
import java.net.URL;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectJobTest {

    @Test
    public void run() throws Exception {
        AgentStatMetricCollector<TAgentStat> agentStatMetricCollector = mockAgentStatMetricCollector();
        Mockito.when(agentStatMetricCollector.collect()).thenReturn(new TAgentStat());

        DataSender dataSender = mock(DataSender.class);

        CollectJob job = new CollectJob(dataSender, "agent", 0, agentStatMetricCollector, 1);
        job.run();

        Mockito.verify(dataSender).send(any(TAgentStatBatch.class));

    }

    @SuppressWarnings("unchecked")
    private AgentStatMetricCollector<TAgentStat> mockAgentStatMetricCollector() {
        return Mockito.mock(AgentStatMetricCollector.class);
    }

    private DefaultApplicationContext newApplicationContext() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        InterceptorRegistryBinder binder = new TestInterceptorRegistryBinder();
        Instrumentation instrumentation = mock(Instrumentation.class);
        AgentOption agentOption = new DefaultAgentOption(instrumentation, "mockAgent", "mockApplicationName", profilerConfig, new URL[0],
                null, new DefaultServiceTypeRegistryService(), new DefaultAnnotationKeyRegistryService());

        return new DefaultApplicationContext(agentOption, binder);
    }



}