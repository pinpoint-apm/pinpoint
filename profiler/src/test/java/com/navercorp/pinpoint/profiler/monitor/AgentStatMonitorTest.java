/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import static org.junit.Assert.*;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.test.ListenableDataSender;
import com.navercorp.pinpoint.test.MockTraceContextFactory;
import com.navercorp.pinpoint.test.TBaseRecorder;
import com.navercorp.pinpoint.test.TBaseRecorderAdaptor;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hyungil.jeong
 */
public class AgentStatMonitorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TBaseRecorder<TAgentStatBatch> tBaseRecorder;
    private DataSender dataSender;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        this.tBaseRecorder = new TBaseRecorder<TAgentStatBatch>();
        TBaseRecorderAdaptor recorderAdaptor = new TBaseRecorderAdaptor(tBaseRecorder);

        ListenableDataSender listenableDataSender = new ListenableDataSender();
        listenableDataSender.setListener(recorderAdaptor);
        this.dataSender = listenableDataSender;
    }

    @Test
    public void testAgentStatMonitor() throws InterruptedException {
        // Given
        final long collectionIntervalMs = 1000 * 1;
        final int numCollectionsPerBatch = 2;
        final int minNumBatchToTest = 2;
        final long totalTestDurationMs = collectionIntervalMs + collectionIntervalMs * numCollectionsPerBatch * minNumBatchToTest;
        // When
        System.setProperty("pinpoint.log", "test.");
        TraceContext testTraceContext = new MockTraceContextFactory().create();
        AgentStatCollectorFactory agentStatCollectorFactory = new AgentStatCollectorFactory(testTraceContext);

        AgentStatMonitor monitor = new AgentStatMonitor(this.dataSender, "agentId", System.currentTimeMillis(),
                agentStatCollectorFactory, collectionIntervalMs, numCollectionsPerBatch);
        monitor.start();
        Thread.sleep(totalTestDurationMs);
        monitor.stop();
        // Then
        assertTrue(tBaseRecorder.size() >= minNumBatchToTest);
        for (TAgentStatBatch agentStatBatch : tBaseRecorder) {
            logger.debug("agentStatBatch:{}", agentStatBatch);
            assertTrue(agentStatBatch.getAgentStats().size() <= numCollectionsPerBatch);
        }
    }

}
