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
 *
 */

package com.navercorp.pinpoint.test.monitor;

import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.DefaultAgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.test.ListenableDataSender;
import com.navercorp.pinpoint.test.TBaseRecorder;
import com.navercorp.pinpoint.test.TBaseRecorderAdaptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class AgentStatMonitorTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private TBaseRecorder<AgentStatMetricSnapshotBatch> tBaseRecorder;
    private DataSender<MetricType> dataSender;

    @Mock
    private AgentStatMetricCollector<AgentStatMetricSnapshot> agentStatCollector;

    @BeforeEach
    public void beforeEach() {
        when(agentStatCollector.collect()).thenReturn(new AgentStatMetricSnapshot());

        this.tBaseRecorder = new TBaseRecorder<>();
        ListenableDataSender.Listener<? extends MetricType> recorderAdaptor = new TBaseRecorderAdaptor<>(tBaseRecorder);

        ListenableDataSender<MetricType> listenableDataSender = new ListenableDataSender<>("testDataSender");
        listenableDataSender.setListener((ListenableDataSender.Listener<MetricType>) recorderAdaptor);
        this.dataSender = listenableDataSender;
    }


    @Test
    public void testAgentStatMonitor() throws InterruptedException {
        // Given
        final long collectionIntervalMs = 1000 * 1;
        final int numCollectionsPerBatch = 2;
        final int minNumBatchToTest = 2;
        final long totalTestDurationMs = collectionIntervalMs + collectionIntervalMs * numCollectionsPerBatch * minNumBatchToTest;

//        profilerConfig.getProfileJvmStatCollectIntervalMs(), profilerConfig.getProfileJvmStatBatchSendCount()

        MonitorConfig mockProfilerConfig = Mockito.mock(MonitorConfig.class);
        Mockito.when(mockProfilerConfig.getProfileJvmStatCollectIntervalMs()).thenReturn((int) collectionIntervalMs);
        Mockito.when(mockProfilerConfig.getProfileJvmStatBatchSendCount()).thenReturn(numCollectionsPerBatch);

        // When
        AgentStatMonitor monitor = new DefaultAgentStatMonitor(this.dataSender, "agentId", System.currentTimeMillis(),
                agentStatCollector, null, null, mockProfilerConfig);
        monitor.start();
        Thread.sleep(totalTestDurationMs);
        monitor.stop();
        // Then
        assertTrue(tBaseRecorder.size() >= minNumBatchToTest);
        for (AgentStatMetricSnapshotBatch agentStatBatch : tBaseRecorder) {
            logger.debug("agentStatBatch:{}", agentStatBatch);
            assertTrue(agentStatBatch.getAgentStats().size() <= numCollectionsPerBatch);
        }
    }
}
