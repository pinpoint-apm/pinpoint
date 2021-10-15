/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.ResponseHistogramsNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseHistograms;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class ApplicationMapBuilderTest {

    private final ExecutorService serialExecutor = Executors.newSingleThreadExecutor();

    private final ExecutorService parallelExecutor = Executors.newFixedThreadPool(8);

    private MapResponseNodeHistogramDataSource mapResponseNodeHistogramDataSource;

    private ResponseHistogramsNodeHistogramDataSource responseHistogramBuilderNodeHistogramDataSource;

    private AgentInfoServerInstanceListDataSource agentInfoServerInstanceListDataSource;

    private long buildTimeoutMillis = 1000;

    @Before
    public void setUp() {
        MapResponseDao mapResponseDao = mock(MapResponseDao.class);
        mapResponseNodeHistogramDataSource = new MapResponseNodeHistogramDataSource(mapResponseDao);

        ResponseHistograms responseHistograms = mock(ResponseHistograms.class);
        responseHistogramBuilderNodeHistogramDataSource = new ResponseHistogramsNodeHistogramDataSource(responseHistograms);

        AgentInfoService agentInfoService = mock(AgentInfoService.class);
        agentInfoServerInstanceListDataSource = new AgentInfoServerInstanceListDataSource(agentInfoService);

        Answer<List<ResponseTime>> responseTimeAnswer = new Answer<List<ResponseTime>>() {
            final long timestamp = System.currentTimeMillis();
            @Override
            public List<ResponseTime> answer(InvocationOnMock invocation) {
                Application application = invocation.getArgument(0);
                String applicationName = application.getName();
                ServiceType applicationServiceType = application.getServiceType();
                int depth = ApplicationMapBuilderTestHelper.getDepthFromApplicationName(applicationName);
                ResponseTime responseTime = new ResponseTime(application.getName(), application.getServiceType(), timestamp);
                responseTime.addResponseTime(ApplicationMapBuilderTestHelper.createAgentIdFromDepth(depth), applicationServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), 1);
                return Collections.singletonList(responseTime);
            }
        };
        when(mapResponseDao.selectResponseTime(any(Application.class), any(Range.class))).thenAnswer(responseTimeAnswer);
        when(responseHistograms.getResponseTimeList(any(Application.class))).thenAnswer(responseTimeAnswer);

        when(agentInfoService.getAgentsByApplicationName(anyString(), anyLong())).thenAnswer(new Answer<Set<AgentInfo>>() {
            @Override
            public Set<AgentInfo> answer(InvocationOnMock invocation) throws Throwable {
                String applicationName = invocation.getArgument(0);
                AgentInfo agentInfo = ApplicationMapBuilderTestHelper.createAgentInfoFromApplicationName(applicationName);
                AgentStatus agentStatus = new AgentStatus(agentInfo.getAgentId(), AgentLifeCycleState.RUNNING, 0);
                agentInfo.setStatus(agentStatus);
                Set<AgentInfo> agentInfos = new HashSet<>();
                agentInfos.add(agentInfo);
                return agentInfos;
            }
        });
        when(agentInfoService.getAgentsByApplicationNameWithoutStatus(anyString(), anyLong())).thenAnswer(new Answer<Set<AgentInfo>>() {
            @Override
            public Set<AgentInfo> answer(InvocationOnMock invocation) throws Throwable {
                String applicationName = invocation.getArgument(0);
                AgentInfo agentInfo = ApplicationMapBuilderTestHelper.createAgentInfoFromApplicationName(applicationName);
                Set<AgentInfo> agentInfos = new HashSet<>();
                agentInfos.add(agentInfo);
                return agentInfos;
            }
        });
        when(agentInfoService.getAgentStatus(anyString(), anyLong())).thenAnswer(new Answer<AgentStatus>()  {
            @Override
            public AgentStatus answer(InvocationOnMock invocation) throws Throwable {
                String agentId = invocation.getArgument(0);
                AgentStatus agentStatus = new AgentStatus(agentId, AgentLifeCycleState.RUNNING, System.currentTimeMillis());
                return agentStatus;
            }
        });
        doAnswer(new Answer<List<Optional<AgentStatus>>>() {
            @Override
            public List<Optional<AgentStatus>> answer(InvocationOnMock invocation) throws Throwable {

                List<Optional<AgentStatus>> result = new ArrayList<>();

                AgentStatusQuery query = invocation.getArgument(0);
                for (SimpleAgentKey agentInfo : query.getAgentKeys()) {
                    AgentStatus agentStatus = new AgentStatus(agentInfo.getAgentId(), AgentLifeCycleState.RUNNING, System.currentTimeMillis());
                    result.add(Optional.of(agentStatus));
                }
                return result;
            }
        }).when(agentInfoService).getAgentStatus(any());
    }

    @After
    public void cleanUp() {
        shutdownExecutor(serialExecutor);
        shutdownExecutor(parallelExecutor);
    }

    private void shutdownExecutor(ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    public void testNoCallData() {
        Range range = Range.newRange(0, 1000);
        Application application = ApplicationMapBuilderTestHelper.createApplicationFromDepth(0);

        ServerInstanceListFactory serverInstanceListFactory = new DefaultServerInstanceListFactory(agentInfoServerInstanceListDataSource);

        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(range, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(range, parallelExecutor);
        ApplicationMap applicationMap = applicationMapBuilder
                .includeServerInfo(serverInstanceListFactory)
                .build(application, buildTimeoutMillis);
        ApplicationMap applicationMap_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeServerInfo(serverInstanceListFactory)
                .build(application, buildTimeoutMillis);

        Assert.assertEquals(1, applicationMap.getNodes().size());
        Assert.assertEquals(1, applicationMap.getNodes().size());
        Assert.assertEquals(1, applicationMap_parallelAppenders.getNodes().size());
        Assert.assertEquals(0, applicationMap.getLinks().size());
        Assert.assertEquals(0, applicationMap.getLinks().size());
        Assert.assertEquals(0, applicationMap_parallelAppenders.getLinks().size());

        ApplicationMapVerifier verifier = new ApplicationMapVerifier(applicationMap);
        verifier.verify(applicationMap);
        verifier.verify(applicationMap_parallelAppenders);
    }

    @Test
    public void testEmptyCallData() {
        Range range = Range.newRange(0, 1000);
        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();

        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(mapResponseNodeHistogramDataSource);
        ServerInstanceListFactory serverInstanceListFactory = new DefaultServerInstanceListFactory(agentInfoServerInstanceListDataSource);

        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(range, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(range, parallelExecutor);
        ApplicationMap applicationMap = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverInstanceListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverInstanceListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);

        Assert.assertTrue(applicationMap.getNodes().isEmpty());
        Assert.assertTrue(applicationMap.getNodes().isEmpty());
        Assert.assertTrue(applicationMap_parallelAppenders.getNodes().isEmpty());
        Assert.assertTrue(applicationMap.getLinks().isEmpty());
        Assert.assertTrue(applicationMap.getLinks().isEmpty());
        Assert.assertTrue(applicationMap_parallelAppenders.getLinks().isEmpty());

        ApplicationMapVerifier verifier = new ApplicationMapVerifier(applicationMap);
        verifier.verify(applicationMap);
        verifier.verify(applicationMap_parallelAppenders);
    }

    /**
     * USER -> WAS(center) -> UNKNOWN
     */
    @Test
    public void testOneDepth() {
        int depth = 1;
        runTest(depth, depth);
    }

    /**
     * USER -> WAS -> WAS(center) -> WAS -> UNKNOWN
     */
    @Test
    public void testTwoDepth() {
        int depth = 2;
        runTest(depth, depth);
    }

    /**
     * USER -> WAS -> WAS -> WAS(center) -> WAS -> WAS -> UNKNOWN
     */
    @Test
    public void testThreeDepth() {
        int depth = 3;
        runTest(depth, depth);
    }

    /**
     * USER -> WAS(center) -> WAS -> WAS -> UNKNOWN
     */
    @Test
    public void test_1_3_depth() {
        int calleeDepth = 1;
        int callerDepth = 3;
        runTest(calleeDepth, callerDepth);
    }

    /**
     * USER -> WAS -> WAS -> WAS(center) -> UNKNOWN
     */
    @Test
    public void test_3_1_depth() {
        int calleeDepth = 3;
        int callerDepth = 1;
        runTest(calleeDepth, callerDepth);
    }

    /**
     * USER -> WAS -> WAS -> WAS(center) -> WAS -> WAS -> WAS -> UNKNOWN
     */
    @Test
    public void test_3_4_depth() {
        int calleeDepth = 3;
        int callerDepth = 4;
        runTest(calleeDepth, callerDepth);
    }

    /**
     * USER -> WAS -> WAS -> WAS -> WAS(center) -> WAS -> WAS -> UNKNOWN
     */
    @Test
    public void test_4_3_depth() {
        int calleeDepth = 4;
        int callerDepth = 3;
        runTest(calleeDepth, callerDepth);
    }

    private void runTest(int callerDepth, int calleeDepth) {
        Range range = Range.newRange(0, 1000);
        int expectedNumNodes = ApplicationMapBuilderTestHelper.getExpectedNumNodes(calleeDepth, callerDepth);
        int expectedNumLinks = ApplicationMapBuilderTestHelper.getExpectedNumLinks(calleeDepth, callerDepth);

        NodeHistogramFactory nodeHistogramFactory_MapResponseDao = new DefaultNodeHistogramFactory(mapResponseNodeHistogramDataSource);
        NodeHistogramFactory nodeHistogramFactory_ResponseHistogramBuilder = new DefaultNodeHistogramFactory(responseHistogramBuilderNodeHistogramDataSource);
        ServerInstanceListFactory serverInstanceListFactory = new DefaultServerInstanceListFactory(agentInfoServerInstanceListDataSource);

        LinkDataDuplexMap linkDataDuplexMap = ApplicationMapBuilderTestHelper.createLinkDataDuplexMap(calleeDepth, callerDepth);
        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(range, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(range, parallelExecutor);

        // test builder using MapResponseDao
        ApplicationMap applicationMap_MapResponseDao = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory_MapResponseDao)
                .includeServerInfo(serverInstanceListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_MapResponseDao_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory_MapResponseDao)
                .includeServerInfo(serverInstanceListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        Assert.assertEquals(expectedNumNodes, applicationMap_MapResponseDao.getNodes().size());
        Assert.assertEquals(expectedNumNodes, applicationMap_MapResponseDao_parallelAppenders.getNodes().size());
        Assert.assertEquals(expectedNumLinks, applicationMap_MapResponseDao.getLinks().size());
        Assert.assertEquals(expectedNumLinks, applicationMap_MapResponseDao_parallelAppenders.getLinks().size());
        ApplicationMapVerifier verifier_MapResponseDao = new ApplicationMapVerifier(applicationMap_MapResponseDao);
        verifier_MapResponseDao.verify(applicationMap_MapResponseDao);
        verifier_MapResponseDao.verify(applicationMap_MapResponseDao_parallelAppenders);

        // test builder using ResponseHistogramBuilder
        ApplicationMap applicationMap_ResponseHistogramBuilder = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory_ResponseHistogramBuilder)
                .includeServerInfo(serverInstanceListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_ResponseHistogramBuilder_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory_ResponseHistogramBuilder)
                .includeServerInfo(serverInstanceListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        Assert.assertEquals(expectedNumNodes, applicationMap_ResponseHistogramBuilder.getNodes().size());
        Assert.assertEquals(expectedNumNodes, applicationMap_ResponseHistogramBuilder_parallelAppenders.getNodes().size());
        Assert.assertEquals(expectedNumLinks, applicationMap_ResponseHistogramBuilder.getLinks().size());
        Assert.assertEquals(expectedNumLinks, applicationMap_ResponseHistogramBuilder_parallelAppenders.getLinks().size());
        ApplicationMapVerifier verifier_ResponseHistogramBuilder = new ApplicationMapVerifier(applicationMap_ResponseHistogramBuilder);
        verifier_ResponseHistogramBuilder.verify(applicationMap_ResponseHistogramBuilder);
        verifier_ResponseHistogramBuilder.verify(applicationMap_ResponseHistogramBuilder_parallelAppenders);
    }
}
