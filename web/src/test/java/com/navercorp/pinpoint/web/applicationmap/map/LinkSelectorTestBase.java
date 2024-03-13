/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.applicationmap.service.LinkDataMapService;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public abstract class LinkSelectorTestBase {

    private static final Random RANDOM = new Random();

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final ApplicationsMapCreatorFactory applicationsMapCreatorFactory = new ApplicationsMapCreatorFactory(executor);

    protected final ServiceType testRpcServiceType = ServiceTypeFactory.of(9000, "TEST_RPC_CLIENT", ServiceTypeProperty.RECORD_STATISTICS);

    protected final Range range = Range.between(0, 100);

    protected LinkDataMapService linkDataMapService;
    protected HostApplicationMapDao hostApplicationMapDao;
    protected LinkSelectorFactory linkSelectorFactory;

    protected abstract LinkSelectorType getLinkSelectorType();

    @BeforeEach
    public void setUp() throws Exception {
        this.linkDataMapService = mock(LinkDataMapService.class);
        this.hostApplicationMapDao = mock(HostApplicationMapDao.class);
        this.linkSelectorFactory = new LinkSelectorFactory(linkDataMapService, applicationsMapCreatorFactory, hostApplicationMapDao, Optional.empty(), () -> LinkDataMapProcessor.NO_OP);
    }

    @AfterEach
    public void cleanUp() {
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

    final LinkDataMap newEmptyLinkDataMap() {
        return new LinkDataMap();
    }

    @Test
    public void testEmpty() {
        Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        when(linkDataMapService.selectCallerLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCalleeLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(Set.of());

        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap select = linkSelector.select(List.of(APP_A), range, 1, 1);

        assertEquals(select.size(), 0);
        assertEquals(select.getTotalCount(), 0);
    }

    @Test
    public void testCaller() {
        // APP_A -> APP_B
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        int callCount_A_B = 10;
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(
                APP_A, "agentA",
                APP_B, "agentB",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);

        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(linkDataMap);
        when(linkDataMapService.selectCalleeLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(Set.of());

        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_A), range, 1, 1);

        assertEquals(linkData.size(), 1);
        assertEquals(linkData.getTotalCount(), callCount_A_B);

        assertThat(linkData.getSourceLinkDataList()).hasSize(1);
        assertEquals(linkData.getSourceLinkDataMap().getTotalCount(), callCount_A_B);

        assertThat(linkData.getTargetLinkDataList()).isEmpty();
    }

    @Test
    public void testCaller_multiple() {
        // APP_A -> TARGET_1, TARGET_2, ...
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        int numTargets = RANDOM.nextInt(100);
        int callCount_A_APP = 4;
        LinkDataMap linkDataMap = new LinkDataMap();
        for (int i = 0; i < numTargets; i++) {
            String targetAppName = "TARGET_" + (i + 1);
            String targetAppAgentId = "target" + (i + 1);
            Application targetApp = new Application(targetAppName, ServiceType.TEST_STAND_ALONE);
            linkDataMap.addLinkData(
                    APP_A, "agentA",
                    targetApp, targetAppAgentId,
                    1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_APP);
        }
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(linkDataMap);
        when(linkDataMapService.selectCalleeLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(Set.of());

        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_A), range, 1, 1);

        assertEquals(linkData.size(), numTargets);
        assertEquals(linkData.getTotalCount(), numTargets * callCount_A_APP);

        assertThat(linkData.getSourceLinkDataList()).hasSize(numTargets);
        assertEquals(linkData.getSourceLinkDataMap().getTotalCount(), numTargets * callCount_A_APP);

        assertThat(linkData.getTargetLinkDataList()).isEmpty();
    }

    @Test
    public void testCaller_3tier() {
        // APP_A -> APP_B -> APP_C
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        final Application APP_C = new Application("APP_C", ServiceType.TEST_STAND_ALONE);
        int callCount_A_B = 10;
        LinkDataMap link_A_B = new LinkDataMap();
        link_A_B.addLinkData(
                APP_A, "agentA",
                APP_B, "agentB",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(link_A_B);

        LinkDataMap link_B_C = new LinkDataMap();
        int callCount_B_C = 20;
        link_B_C.addLinkData(
                APP_B, "agentB",
                APP_C, "agentC",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_B_C);
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(link_B_C);

        when(linkDataMapService.selectCalleeLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(Set.of());

        // depth 1
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_A), range, 1, 1);

        assertEquals(linkData.size(), 1);
        assertEquals(linkData.getTotalCount(), callCount_A_B);

        assertThat(linkData.getSourceLinkDataList()).hasSize(1);
        assertEquals(linkData.getSourceLinkDataMap().getTotalCount(), callCount_A_B);
        assertSource_Target_TotalCount("APP_A->APP_B", linkData, new LinkKey(APP_A, APP_B), callCount_A_B);

        assertThat(linkData.getTargetLinkDataList()).isEmpty();

        // depth 2
        LinkSelector linkSelector2 = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData_depth2 = linkSelector2.select(List.of(APP_A), range, 2, 2);
        assertEquals(linkData_depth2.size(), 2);
        assertEquals(linkData_depth2.getTotalCount(), callCount_A_B + callCount_B_C);

        LinkKey linkKey_A_B = new LinkKey(APP_A, APP_B);
        assertSource_Target_TotalCount("APP_A->APP_B", linkData_depth2, linkKey_A_B, callCount_A_B);

        LinkKey linkKey_B_C = new LinkKey(APP_B, APP_C);
        assertSource_Target_TotalCount("APP_B->APP_C", linkData_depth2, linkKey_B_C, callCount_B_C);
    }

    @Test
    public void testCaller_rpc() {
        // APP_A -> APP_B via "www.test.com/test"
        // Given
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        final String rpcUri = "www.test.com/test";
        final Application RPC_A_B = new Application(rpcUri, testRpcServiceType);
        final Set<AcceptApplication> acceptApplications = Set.of(new AcceptApplication(rpcUri, APP_B));
        int callCount_A_B = 10;
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(
                APP_A, "agentA",
                RPC_A_B, rpcUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);

        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(linkDataMap);
        when(linkDataMapService.selectCalleeLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(eq(APP_A), any(Range.class))).thenReturn(acceptApplications);

        // When
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_A), range, 1, 1);

        // Then
        assertEquals(1, linkData.size());
        assertEquals(callCount_A_B, linkData.getTotalCount());

        assertThat(linkData.getSourceLinkDataList()).hasSize(1);
        assertEquals(callCount_A_B, linkData.getSourceLinkDataMap().getTotalCount());

        assertThat(linkData.getTargetLinkDataList()).isEmpty();

        LinkData linkData_A_B = linkData.getSourceLinkData(new LinkKey(APP_A, APP_B));
        assertEquals(callCount_A_B, linkData_A_B.getTotalCount());

        List<LinkCallData> callDatas = List.copyOf(linkData_A_B.getLinkCallDataMap().getLinkDataList());
        assertThat(callDatas).hasSize(1);
        LinkCallData callData = callDatas.get(0);
        assertEquals(rpcUri, callData.getTarget().name());
        assertEquals(testRpcServiceType, callData.getTarget().serviceType());
    }

    @Test
    public void testCallee() {
        // APP_A -> APP_B
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        int callCount_A_B = 10;
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(
                APP_A, "agentA",
                APP_B, "agentB",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);

        when(linkDataMapService.selectCallerLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(linkDataMap);
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(Set.of());

        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_B), range, 1, 1);

        assertEquals(linkData.size(), 1);
        assertEquals(linkData.getTotalCount(), callCount_A_B);

        assertThat(linkData.getSourceLinkDataList()).isEmpty();

        assertThat(linkData.getTargetLinkDataList()).hasSize(1);
        assertEquals(linkData.getTargetLinkDataMap().getTotalCount(), callCount_A_B);
    }

    @Test
    public void testCallee_3tier() {
        // APP_A -> APP_B -> APP_C
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        final Application APP_C = new Application("APP_C", ServiceType.TEST_STAND_ALONE);
        when(linkDataMapService.selectCalleeLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());

        int callCount_A_B = 30;
        LinkDataMap linkDataMap_A_B = new LinkDataMap();
        linkDataMap_A_B.addLinkData(
                APP_A, "agentA",
                APP_B, "agentB",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(linkDataMap_A_B);

        int callCount_B_C = 40;
        LinkDataMap linkDataMap_B_C = new LinkDataMap();
        linkDataMap_B_C.addLinkData(
                APP_B, "agentB",
                APP_C, "agentC",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_B_C);
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_C), any(Range.class), anyBoolean())).thenReturn(linkDataMap_B_C);

        when(linkDataMapService.selectCallerLinkDataMap(any(Application.class), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(Set.of());

        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_C), range, 1, 1);

        assertEquals(linkData.size(), 1);
        assertEquals(linkData.getTotalCount(), callCount_B_C);

        assertThat(linkData.getSourceLinkDataList()).isEmpty();

        assertThat(linkData.getTargetLinkDataList()).hasSize(1);
        assertEquals(linkData.getTotalCount(), callCount_B_C);

        // depth 2
        LinkSelector linkSelector2 = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData_depth2 = linkSelector2.select(List.of(APP_C), range, 2, 2);
        assertEquals(linkData_depth2.size(), 2);

        LinkKey linkKey_A_B = new LinkKey(APP_A, APP_B);
        assertTarget_Source_TotalCount("APP_A->APP_B", linkData_depth2, linkKey_A_B, callCount_A_B);

        LinkKey linkKey_B_C = new LinkKey(APP_B, APP_C);
        assertTarget_Source_TotalCount("APP_B->APP_C", linkData_depth2, linkKey_B_C, callCount_B_C);
    }

    @Test
    public void testVirtual() {
        // APP_A ---> APP_B via "www.test.com/test"
        //        |-> APP_C via "www.test.com/test"
        // Given
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        final Application APP_C = new Application("APP_C", ServiceType.TEST_STAND_ALONE);
        final String rpcUri = "www.test.com/test";
        final Application RPC_A = new Application(rpcUri, testRpcServiceType);
        final Set<AcceptApplication> acceptApplications = Set.of(new AcceptApplication(rpcUri, APP_B), new AcceptApplication(rpcUri, APP_C));

        int callCount_A_B = 10;
        int callCount_A_C = 20;
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(
                APP_A, "agentA",
                RPC_A, rpcUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B + callCount_A_C);

        LinkDataMap rpc_A_B_calleeLinkDataMap = new LinkDataMap();
        rpc_A_B_calleeLinkDataMap.addLinkData(
                APP_A, "agentA",
                APP_B, "agentB",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);
        LinkDataMap rpc_A_C_calleeLinkDataMap = new LinkDataMap();
        rpc_A_C_calleeLinkDataMap.addLinkData(
                APP_A, "agentA",
                APP_C, "agentC",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_C);

        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(linkDataMap);
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(rpc_A_B_calleeLinkDataMap);
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_C), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_C), any(Range.class), anyBoolean())).thenReturn(rpc_A_C_calleeLinkDataMap);
        when(hostApplicationMapDao.findAcceptApplicationName(eq(APP_A), any(Range.class))).thenReturn(acceptApplications);

        // When
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_A), range, 1, 1);

        // Then
        LinkData linkData_A_B = linkData.getSourceLinkData(new LinkKey(APP_A, APP_B));
        LinkData linkData_A_C = linkData.getSourceLinkData(new LinkKey(APP_A, APP_C));
        assertEquals(callCount_A_B, linkData_A_B.getTotalCount());
        assertEquals(callCount_A_C, linkData_A_C.getTotalCount());

        List<LinkCallData> callData_A_Bs = List.copyOf(linkData_A_B.getLinkCallDataMap().getLinkDataList());
        assertThat(callData_A_Bs).hasSize(1);
        LinkCallData callData_A_B = callData_A_Bs.get(0);
        assertEquals(rpcUri, callData_A_B.getTarget().name());
        assertEquals(testRpcServiceType, callData_A_B.getTarget().serviceType());

        List<LinkCallData> callData_A_Cs = List.copyOf(linkData_A_C.getLinkCallDataMap().getLinkDataList());
        assertThat(callData_A_Cs).hasSize(1);
        LinkCallData callData_A_C = callData_A_Cs.get(0);
        assertEquals(rpcUri, callData_A_C.getTarget().name());
        assertEquals(testRpcServiceType, callData_A_C.getTarget().serviceType());
    }

    @Test
    public void testVirtual_mixed() {
        // APP_A ---> APP_B via "api.test.com/test", "b.test.com/test"
        //        |-> APP_C via "api.test.com/test", "c.test.com/test"
        // Given
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        final Application APP_C = new Application("APP_C", ServiceType.TEST_STAND_ALONE);
        final String proxyUri = "api.test.com/test";
        final String bUri = "b.test.com/test";
        final String cUri = "c.test.com/test";
        final Application RPC_PROXY = new Application(proxyUri, testRpcServiceType);
        final Application RPC_B = new Application(bUri, testRpcServiceType);
        final Application RPC_C = new Application(cUri, testRpcServiceType);
        final Set<AcceptApplication> acceptApplications = Set.of(
                new AcceptApplication(proxyUri, APP_B),
                new AcceptApplication(proxyUri, APP_C),
                new AcceptApplication(bUri, APP_B),
                new AcceptApplication(cUri, APP_C)
        );

        int callCount_proxy_B = 10;
        int callCount_proxy_C = 20;
        int callCount_B = 4;
        int callCount_C = 7;
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(
                APP_A, "agentA",
                RPC_PROXY, proxyUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_proxy_B + callCount_proxy_C);
        linkDataMap.addLinkData(
                APP_A, "agentA",
                RPC_B, bUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_B);
        linkDataMap.addLinkData(
                APP_A, "agentA",
                RPC_C, cUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_C);

        LinkDataMap calleeLinkDataMap_B = new LinkDataMap();
        calleeLinkDataMap_B.addLinkData(
                APP_A, "agentA",
                APP_B, "agentB",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_proxy_B + callCount_B);
        LinkDataMap calleeLinkDataMap_C = new LinkDataMap();
        calleeLinkDataMap_C.addLinkData(
                APP_A, "agentA",
                APP_C, "agentC",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_proxy_C + callCount_C);

        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(linkDataMap);
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(calleeLinkDataMap_B);
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_C), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_C), any(Range.class), anyBoolean())).thenReturn(calleeLinkDataMap_C);
        when(hostApplicationMapDao.findAcceptApplicationName(eq(APP_A), any(Range.class))).thenReturn(acceptApplications);

        // When
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_A), range, 1, 1);

        // Then
        assertSource_Target_TotalCount("APP_A -> APP_B", linkData, new LinkKey(APP_A, APP_B), callCount_proxy_B + callCount_B);
        assertSource_Target_TotalCount("APP_A -> APP_C", linkData, new LinkKey(APP_A, APP_C), callCount_proxy_C + callCount_C);
        assertTarget_Source_TotalCount("APP_A -> APP_B", linkData, new LinkKey(APP_A, APP_B), callCount_proxy_B + callCount_B);
        assertTarget_Source_TotalCount("APP_A -> APP_C", linkData, new LinkKey(APP_A, APP_C), callCount_proxy_C + callCount_C);
    }

    /**
     * For situations where virtual nodes are visited, but their callee data are not fetched due to callee search limit.
     */
    @Test
    public void testVirtual_3tier_callee_limited() {
        // APP_A ---> APP_B via "gw.test.com/api" ---> APP_D via "api.test.com/test
        //        |-> APP_C via "gw.test.com/api" -|
        // Given
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);
        final Application APP_B = new Application("APP_B", ServiceType.TEST_STAND_ALONE);
        final Application APP_C = new Application("APP_C", ServiceType.TEST_STAND_ALONE);
        final Application APP_D = new Application("APP_D", ServiceType.TEST_STAND_ALONE);
        final String gwUri = "gw.test.com/api";
        final String apiUri = "api.test.com/test";
        final Application RPC_GW = new Application(gwUri, testRpcServiceType);
        final Application RPC_API = new Application(apiUri, testRpcServiceType);
        final Set<AcceptApplication> gwAcceptApplications = Set.of(new AcceptApplication(gwUri, APP_B), new AcceptApplication(gwUri, APP_C));
        final Set<AcceptApplication> apiAcceptApplications = Set.of(new AcceptApplication(apiUri, APP_D));

        final int callCount_A_B = 4, callCount_A_C = 6;
        final int callCount_B_D = 4, callCount_C_D = 6;
        LinkDataMap callerlinkDataMap_A = new LinkDataMap();
        callerlinkDataMap_A.addLinkData(
                APP_A, "agentA",
                RPC_GW, gwUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);
        LinkDataMap callerLinkDataMap_B = new LinkDataMap();
        callerLinkDataMap_B.addLinkData(
                APP_B, "agentB",
                RPC_API, apiUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_B_D);
        LinkDataMap callerLinkDataMap_C = new LinkDataMap();
        callerLinkDataMap_C.addLinkData(
                APP_C, "agentC",
                RPC_API, apiUri,
                1000, testRpcServiceType.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_C_D);

        LinkDataMap calleeLinkDataMap_B = new LinkDataMap();
        calleeLinkDataMap_B.addLinkData(
                APP_A, "agentA",
                APP_B, "agentB",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_B);
        LinkDataMap calleeLinkDataMap_C = new LinkDataMap();
        calleeLinkDataMap_C.addLinkData(
                APP_A, "agentA",
                APP_C, "agentC",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_A_C);
        LinkDataMap calleeLinkDataMap_D = new LinkDataMap();
        calleeLinkDataMap_D.addLinkData(
                APP_B, "agentB",
                APP_D, "agentD",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_B_D);
        calleeLinkDataMap_D.addLinkData(
                APP_C, "agentC",
                APP_D, "agentD",
                1000, ServiceType.TEST_STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount_C_D);

        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(callerlinkDataMap_A);
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_A), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(callerLinkDataMap_B);
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_B), any(Range.class), anyBoolean())).thenReturn(calleeLinkDataMap_B);
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_C), any(Range.class), anyBoolean())).thenReturn(callerLinkDataMap_C);
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_C), any(Range.class), anyBoolean())).thenReturn(calleeLinkDataMap_C);
        when(linkDataMapService.selectCallerLinkDataMap(eq(APP_D), any(Range.class), anyBoolean())).thenReturn(newEmptyLinkDataMap());
        when(linkDataMapService.selectCalleeLinkDataMap(eq(APP_D), any(Range.class), anyBoolean())).thenReturn(calleeLinkDataMap_D);
        when(hostApplicationMapDao.findAcceptApplicationName(eq(APP_A), any(Range.class))).thenReturn(gwAcceptApplications);
        when(hostApplicationMapDao.findAcceptApplicationName(eq(APP_B), any(Range.class))).thenReturn(apiAcceptApplications);
        when(hostApplicationMapDao.findAcceptApplicationName(eq(APP_C), any(Range.class))).thenReturn(apiAcceptApplications);

        // When
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkData = linkSelector.select(List.of(APP_A), range, 2, 1);

        // Then
        LinkData linkData_A_B = linkData.getSourceLinkData(new LinkKey(APP_A, APP_B));
        LinkData linkData_A_C = linkData.getSourceLinkData(new LinkKey(APP_A, APP_C));
        assertEquals(callCount_A_B, linkData_A_B.getTotalCount());
        assertEquals(callCount_A_C, linkData_A_C.getTotalCount());
        LinkData linkData_B_D = linkData.getSourceLinkData(new LinkKey(APP_B, APP_D));
        assertEquals(callCount_B_D, linkData_B_D.getTotalCount());
        LinkData linkData_C_D = linkData.getSourceLinkData(new LinkKey(APP_C, APP_D));
        assertEquals(callCount_C_D, linkData_C_D.getTotalCount());

        LinkData targetLinkData_A_B = linkData.getTargetLinkData(new LinkKey(APP_A, APP_B));
        LinkData targetLinkData_A_C = linkData.getTargetLinkData(new LinkKey(APP_A, APP_C));
        assertEquals(callCount_A_B, targetLinkData_A_B.getTotalCount());
        assertEquals(callCount_A_C, targetLinkData_A_C.getTotalCount());
    }

    private void assertTarget_Source_TotalCount(String message, LinkDataDuplexMap linkData, LinkKey linkKey, long count) {
        LinkData sourceLinkData = linkData.getTargetLinkData(linkKey);
        long totalCount = sourceLinkData.getTotalCount();
        assertEquals(totalCount, count, message);
    }

    private void assertSource_Target_TotalCount(String message, LinkDataDuplexMap linkData, LinkKey linkKey, long count) {
        LinkData sourceLinkData = linkData.getSourceLinkData(linkKey);
        long totalCount = sourceLinkData.getTotalCount();
        assertEquals(totalCount, count, message);
    }
}
