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

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.profiler.context.AtomicIdGenerator;
import com.navercorp.pinpoint.profiler.context.DefaultTransactionCounter;
import com.navercorp.pinpoint.profiler.context.TransactionCounter;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.monitor.DatabaseInfoCache;
import com.navercorp.pinpoint.profiler.context.monitor.DefaultPluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.DefaultAgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.monitor.codahale.DefaultAgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.test.ListenableDataSender;
import com.navercorp.pinpoint.test.TBaseRecorder;
import com.navercorp.pinpoint.test.TBaseRecorderAdaptor;
import com.navercorp.pinpoint.thrift.dto.TAgentStatBatch;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static org.junit.Assert.assertTrue;

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

        ListenableDataSender listenableDataSender = new ListenableDataSender("testDataSender");
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
        AgentStatCollectorFactory agentStatCollectorFactory = newAgentStatCollectorFactory();

        AgentStatMonitor monitor = new DefaultAgentStatMonitor(this.dataSender, "agentId", System.currentTimeMillis(),
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

    private AgentStatCollectorFactory newAgentStatCollectorFactory() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        ActiveTraceRepository activeTraceRepository = new ActiveTraceRepository();
        AtomicIdGenerator idGenerator = new AtomicIdGenerator();
        TransactionCounter transactionCounter = new DefaultTransactionCounter(idGenerator);
        PluginMonitorContext pluginMonitorContext = new DefaultPluginMonitorContext();
        return new DefaultAgentStatCollectorFactory(profilerConfig, activeTraceRepository, transactionCounter, pluginMonitorContext, new DatabaseInfoCache(Collections.<JdbcUrlParser>emptyList()));
    }

}
