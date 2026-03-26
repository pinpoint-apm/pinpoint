/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.SimplifiedNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseSimplifiedNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.ResponseHistogramsNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AgentListV2Service;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseHistograms;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
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

    @AutoClose("shutdown")
    private static final Executor serialExecutor = Executors.newSingleThreadExecutor();

    @AutoClose("shutdown")
    private static final Executor parallelExecutor = Executors.newFixedThreadPool(8);

    private MapResponseNodeHistogramDataSource mapResponseNodeHistogramDataSource;

    private MapResponseSimplifiedNodeHistogramDataSource mapResponseSimplifiedNodeHistogramDataSource;

    private ResponseHistogramsNodeHistogramDataSource responseHistogramBuilderNodeHistogramDataSource;

    private AgentInfoServerGroupListDataSource agentInfoServerGroupListDataSource;

    private AgentInfoServerGroupListDataSource agentInfoServerGroupListDataSourceV2;

    private final long buildTimeoutMillis = 1000;

    @BeforeEach
    public void setUp() {
        MapAgentResponseDao mapAgentResponseDao = mock(MapAgentResponseDao.class);
        mapResponseNodeHistogramDataSource = new MapResponseNodeHistogramDataSource(mapAgentResponseDao);
        mapResponseSimplifiedNodeHistogramDataSource = new MapResponseSimplifiedNodeHistogramDataSource(mapAgentResponseDao);

        ResponseHistograms responseHistograms = mock(ResponseHistograms.class);
        responseHistogramBuilderNodeHistogramDataSource = new ResponseHistogramsNodeHistogramDataSource(responseHistograms);

        AgentInfoService agentInfoService = mock(AgentInfoService.class);
        AgentListV2Service agentListV2Service = mock(AgentListV2Service.class);
        agentInfoServerGroupListDataSource = new AgentInfoServerGroupListDataSource(agentInfoService, agentListV2Service, false);
        agentInfoServerGroupListDataSourceV2 = new AgentInfoServerGroupListDataSource(agentInfoService, agentListV2Service, true);

        Answer<List<ResponseTime>> responseTimeAnswer = new Answer<>() {
            final long timestamp = System.currentTimeMillis();
            @Override
            public List<ResponseTime> answer(InvocationOnMock invocation) {
                Application application = invocation.getArgument(0);
                String applicationName = application.getApplicationName();
                ServiceType applicationServiceType = application.getServiceType();
                int depth = ApplicationMapBuilderTestHelper.getDepthFromApplicationName(applicationName);
                ResponseTime.Builder responseTimeBuilder = ResponseTime.newBuilder(application.getApplicationName(), application.getServiceType(), timestamp);
                responseTimeBuilder.addResponseTime(ApplicationMapBuilderTestHelper.createAgentIdFromDepth(depth), applicationServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), 1);
                return List.of(responseTimeBuilder.build());
            }
        };
        when(mapAgentResponseDao.selectResponseTime(any(Application.class), any(TimeWindow.class))).thenAnswer(responseTimeAnswer);
        when(responseHistograms.getResponseTimeList(any(Application.class))).thenAnswer(responseTimeAnswer);

        when(agentInfoService.getAgentInfoByApplicationName(anyString(), anyLong())).thenAnswer(new Answer<>() {
            @Override
            public List<AgentInfo> answer(InvocationOnMock invocation) throws Throwable {
                String applicationName = invocation.getArgument(0);
                AgentInfo agentInfo = ApplicationMapBuilderTestHelper.createAgentInfoFromApplicationName(applicationName);
                return List.of(agentInfo);
            }
        });
        when(agentInfoService.findAgentStatus(anyString(), anyLong())).thenAnswer(new Answer<>()  {
            @Override
            public AgentStatus answer(InvocationOnMock invocation) throws Throwable {
                String agentId = invocation.getArgument(0);
                return new AgentStatus(agentId, AgentLifeCycleState.RUNNING, System.currentTimeMillis());
            }
        });
        doAnswer(new Answer<List<Optional<AgentStatus>>>() {
            @Override
            public List<Optional<AgentStatus>> answer(InvocationOnMock invocation) throws Throwable {

                List<Optional<AgentStatus>> result = new ArrayList<>();

                AgentStatusQuery query = invocation.getArgument(0);
                for (SimpleAgentKey agentInfo : query.getAgentKeys()) {
                    AgentStatus agentStatus = new AgentStatus(agentInfo.agentId(), AgentLifeCycleState.RUNNING, System.currentTimeMillis());
                    result.add(Optional.of(agentStatus));
                }
                return result;
            }
        }).when(agentInfoService).getAgentStatus(any());

        when(agentListV2Service.getActiveAgentList(any(ServiceUid.class), anyString(), any(ServiceType.class), any(Range.class))).thenAnswer(invocation -> {
            String applicationName = invocation.getArgument(1);
            ServiceType serviceType = invocation.getArgument(2);
            Range range = invocation.getArgument(3);
            int depth = ApplicationMapBuilderTestHelper.getDepthFromApplicationName(applicationName);
            String agentId = ApplicationMapBuilderTestHelper.createAgentIdFromDepth(depth);
            Application application = new Application(applicationName, serviceType);
            AgentIdEntry entry = new AgentIdEntry(application, agentId, 1000L, agentId, AgentLifeCycleState.RUNNING, range.getTo());
            return List.of(entry);
        });
        when(agentInfoService.getAgentInfos(any())).thenAnswer(invocation -> {
            List<SimpleAgentKey> keys = invocation.getArgument(0);
            List<AgentInfo> result = new ArrayList<>();
            for (SimpleAgentKey key : keys) {
                int depth = ApplicationMapBuilderTestHelper.getDepthFromApplicationName(key.agentId());
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.setAgentId(key.agentId());
                agentInfo.setHostName(ApplicationMapBuilderTestHelper.createHostnameFromDepth(depth));
                result.add(agentInfo);
            }
            return result;
        });
    }

    @Test
    public void testNoCallData() {
        Range range = Range.between(0, 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        Application application = ApplicationMapBuilderTestHelper.createApplicationFromDepth(0);

        ServerGroupListFactory serverGroupListFactory = new DefaultServerGroupListFactory(agentInfoServerGroupListDataSource);

        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, parallelExecutor);
        ApplicationMap applicationMap = applicationMapBuilder
                .includeServerInfo(serverGroupListFactory)
                .buildForEmptyApplication(application, buildTimeoutMillis);
        ApplicationMap applicationMap_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeServerInfo(serverGroupListFactory)
                .buildForEmptyApplication(application, buildTimeoutMillis);

        assertThat(applicationMap.getNodes().getNodeList()).hasSize(1);
        assertThat(applicationMap_parallelAppenders.getNodes().getNodeList()).hasSize(1);
        assertThat(applicationMap.getLinks().getLinkList()).isEmpty();
        assertThat(applicationMap_parallelAppenders.getLinks().getLinkList()).isEmpty();

        ApplicationMapVerifier verifier = new ApplicationMapVerifier(applicationMap);
        verifier.verify(applicationMap);
        verifier.verify(applicationMap_parallelAppenders);
    }

    @Test
    public void testEmptyCallData() {
        Range range = Range.between(0, 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();

        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(mapResponseNodeHistogramDataSource);
        ServerGroupListFactory serverGroupListFactory = new DefaultServerGroupListFactory(agentInfoServerGroupListDataSource);

        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, parallelExecutor);
        ApplicationMap applicationMap = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);

        assertThat(applicationMap.getNodes().getNodeList()).isEmpty();
        assertThat(applicationMap_parallelAppenders.getNodes().getNodeList()).isEmpty();

        assertThat(applicationMap.getLinks().getLinkList()).isEmpty();
        assertThat(applicationMap_parallelAppenders.getLinks().getLinkList()).isEmpty();

        ApplicationMapVerifier verifier = new ApplicationMapVerifier(applicationMap);
        verifier.verify(applicationMap);
        verifier.verify(applicationMap_parallelAppenders);
    }

    @Test
    public void testEmptyCallDataSimplified() {
        Range range = Range.between(0, 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();

        NodeHistogramFactory nodeHistogramFactory = new SimplifiedNodeHistogramFactory(mapResponseSimplifiedNodeHistogramDataSource);
        ServerGroupListFactory serverGroupListFactory = new DefaultServerGroupListFactory(agentInfoServerGroupListDataSource);

        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, parallelExecutor);
        ApplicationMap applicationMap = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);

        assertThat(applicationMap.getNodes().getNodeList()).isEmpty();
        assertThat(applicationMap_parallelAppenders.getNodes().getNodeList()).isEmpty();

        assertThat(applicationMap.getLinks().getLinkList()).isEmpty();
        assertThat(applicationMap_parallelAppenders.getLinks().getLinkList()).isEmpty();

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

    @Test
    public void testOneDepthSimplified() {
        int depth = 1;
        runTestSimplified(depth, depth);
    }

    /**
     * USER -> WAS -> WAS(center) -> WAS -> UNKNOWN
     */
    @Test
    public void testTwoDepth() {
        int depth = 2;
        runTest(depth, depth);
    }

    @Test
    public void testTwoDepthSimplified() {
        int depth = 2;
        runTestSimplified(depth, depth);
    }

    /**
     * USER -> WAS -> WAS -> WAS(center) -> WAS -> WAS -> UNKNOWN
     */
    @Test
    public void testThreeDepth() {
        int depth = 3;
        runTest(depth, depth);
    }

    @Test
    public void testThreeDepthSimplified() {
        int depth = 3;
        runTestSimplified(depth, depth);
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

    @Test
    public void test_1_3_depthSimplified() {
        int calleeDepth = 1;
        int callerDepth = 3;
        runTestSimplified(calleeDepth, callerDepth);
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

    @Test
    public void test_3_1_depthSimplified() {
        int calleeDepth = 3;
        int callerDepth = 1;
        runTestSimplified(calleeDepth, callerDepth);
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

    @Test
    public void test_3_4_depthSimplified() {
        int calleeDepth = 3;
        int callerDepth = 4;
        runTestSimplified(calleeDepth, callerDepth);
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

    @Test
    public void test_4_3_depthSimplified() {
        int calleeDepth = 4;
        int callerDepth = 3;
        runTestSimplified(calleeDepth, callerDepth);
    }

    private void runTest(int callerDepth, int calleeDepth) {
        Range range = Range.between(0, 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        int expectedNumNodes = ApplicationMapBuilderTestHelper.getExpectedNumNodes(calleeDepth, callerDepth);
        int expectedNumLinks = ApplicationMapBuilderTestHelper.getExpectedNumLinks(calleeDepth, callerDepth);

        NodeHistogramFactory nodeHistogramFactory_MapResponseDao = new DefaultNodeHistogramFactory(mapResponseNodeHistogramDataSource);
        NodeHistogramFactory nodeHistogramFactory_ResponseHistogramBuilder = new DefaultNodeHistogramFactory(responseHistogramBuilderNodeHistogramDataSource);
        ServerGroupListFactory serverGroupListFactory = new DefaultServerGroupListFactory(agentInfoServerGroupListDataSource);

        LinkDataDuplexMap linkDataDuplexMap = ApplicationMapBuilderTestHelper.createLinkDataDuplexMap(calleeDepth, callerDepth);
        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, parallelExecutor);

        // test builder using MapResponseDao
        ApplicationMap applicationMap_MapResponseDao = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory_MapResponseDao)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_MapResponseDao_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory_MapResponseDao)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);

        assertThat(applicationMap_MapResponseDao.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap_MapResponseDao_parallelAppenders.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap_MapResponseDao.getLinks().getLinkList()).hasSize(expectedNumLinks);
        assertThat(applicationMap_MapResponseDao_parallelAppenders.getLinks().getLinkList()).hasSize(expectedNumLinks);

        ApplicationMapVerifier verifier_MapResponseDao = new ApplicationMapVerifier(applicationMap_MapResponseDao);
        verifier_MapResponseDao.verify(applicationMap_MapResponseDao);
        verifier_MapResponseDao.verify(applicationMap_MapResponseDao_parallelAppenders);

        // test builder using ResponseHistogramBuilder
        ApplicationMap applicationMap_ResponseHistogramBuilder = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory_ResponseHistogramBuilder)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_ResponseHistogramBuilder_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory_ResponseHistogramBuilder)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);

        assertThat(applicationMap_ResponseHistogramBuilder.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap_ResponseHistogramBuilder_parallelAppenders.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap_ResponseHistogramBuilder.getLinks().getLinkList()).hasSize(expectedNumLinks);
        assertThat(applicationMap_ResponseHistogramBuilder_parallelAppenders.getLinks().getLinkList()).hasSize(expectedNumLinks);

        ApplicationMapVerifier verifier_ResponseHistogramBuilder = new ApplicationMapVerifier(applicationMap_ResponseHistogramBuilder);
        verifier_ResponseHistogramBuilder.verify(applicationMap_ResponseHistogramBuilder);
        verifier_ResponseHistogramBuilder.verify(applicationMap_ResponseHistogramBuilder_parallelAppenders);
    }

    private void runTestSimplified(int callerDepth, int calleeDepth) {
        Range range = Range.between(0, 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        int expectedNumNodes = ApplicationMapBuilderTestHelper.getExpectedNumNodes(calleeDepth, callerDepth);
        int expectedNumLinks = ApplicationMapBuilderTestHelper.getExpectedNumLinks(calleeDepth, callerDepth);

        NodeHistogramFactory nodeHistogramFactory_MapResponseDao = new SimplifiedNodeHistogramFactory(mapResponseSimplifiedNodeHistogramDataSource);
        ServerGroupListFactory serverGroupListFactory = new DefaultServerGroupListFactory(agentInfoServerGroupListDataSource);

        LinkDataDuplexMap linkDataDuplexMap = ApplicationMapBuilderTestHelper.createLinkDataDuplexMap(calleeDepth, callerDepth);
        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, parallelExecutor);

        // test builder using MapResponseDao
        ApplicationMap applicationMap_MapResponseDao = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory_MapResponseDao)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_MapResponseDao_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory_MapResponseDao)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);

        assertThat(applicationMap_MapResponseDao.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap_MapResponseDao_parallelAppenders.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap_MapResponseDao.getLinks().getLinkList()).hasSize(expectedNumLinks);
        assertThat(applicationMap_MapResponseDao_parallelAppenders.getLinks().getLinkList()).hasSize(expectedNumLinks);

        ApplicationMapVerifier verifier_MapResponseDao = new ApplicationMapVerifier(applicationMap_MapResponseDao);
        verifier_MapResponseDao.verify(applicationMap_MapResponseDao);
        verifier_MapResponseDao.verify(applicationMap_MapResponseDao_parallelAppenders);
    }

    @Test
    public void testNoCallData_agentReadV2() {
        Range range = Range.between(0, 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        Application application = ApplicationMapBuilderTestHelper.createApplicationFromDepth(0);

        ServerGroupListFactory serverGroupListFactory = new DefaultServerGroupListFactory(agentInfoServerGroupListDataSourceV2);

        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, parallelExecutor);
        ApplicationMap applicationMap = applicationMapBuilder
                .includeServerInfo(serverGroupListFactory)
                .buildForEmptyApplication(application, buildTimeoutMillis);
        ApplicationMap applicationMap_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeServerInfo(serverGroupListFactory)
                .buildForEmptyApplication(application, buildTimeoutMillis);

        assertThat(applicationMap.getNodes().getNodeList()).hasSize(1);
        assertThat(applicationMap_parallelAppenders.getNodes().getNodeList()).hasSize(1);
        assertThat(applicationMap.getLinks().getLinkList()).isEmpty();
        assertThat(applicationMap_parallelAppenders.getLinks().getLinkList()).isEmpty();

        ApplicationMapVerifier verifier = new ApplicationMapVerifier(applicationMap);
        verifier.verify(applicationMap);
        verifier.verify(applicationMap_parallelAppenders);
    }

    @Test
    public void testOneDepth_agentReadV2() {
        int depth = 1;
        runTestV2(depth, depth);
    }

    @Test
    public void testTwoDepth_agentReadV2() {
        int depth = 2;
        runTestV2(depth, depth);
    }

    private void runTestV2(int callerDepth, int calleeDepth) {
        Range range = Range.between(0, 1000);
        TimeWindow timeWindow = new TimeWindow(range);

        int expectedNumNodes = ApplicationMapBuilderTestHelper.getExpectedNumNodes(calleeDepth, callerDepth);
        int expectedNumLinks = ApplicationMapBuilderTestHelper.getExpectedNumLinks(calleeDepth, callerDepth);

        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(mapResponseNodeHistogramDataSource);
        ServerGroupListFactory serverGroupListFactory = new DefaultServerGroupListFactory(agentInfoServerGroupListDataSourceV2);

        LinkDataDuplexMap linkDataDuplexMap = ApplicationMapBuilderTestHelper.createLinkDataDuplexMap(calleeDepth, callerDepth);
        ApplicationMapBuilder applicationMapBuilder = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, serialExecutor);
        ApplicationMapBuilder applicationMapBuilder_parallelAppenders = ApplicationMapBuilderTestHelper.createApplicationMapBuilder(timeWindow, parallelExecutor);

        ApplicationMap applicationMap = applicationMapBuilder
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);
        ApplicationMap applicationMap_parallelAppenders = applicationMapBuilder_parallelAppenders
                .includeNodeHistogram(nodeHistogramFactory)
                .includeServerInfo(serverGroupListFactory)
                .build(linkDataDuplexMap, buildTimeoutMillis);

        assertThat(applicationMap.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap_parallelAppenders.getNodes().getNodeList()).hasSize(expectedNumNodes);
        assertThat(applicationMap.getLinks().getLinkList()).hasSize(expectedNumLinks);
        assertThat(applicationMap_parallelAppenders.getLinks().getLinkList()).hasSize(expectedNumLinks);

        ApplicationMapVerifier verifier = new ApplicationMapVerifier(applicationMap);
        verifier.verify(applicationMap);
        verifier.verify(applicationMap_parallelAppenders);
    }
}

