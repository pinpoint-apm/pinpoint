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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.*;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author emeroad
 */
public class BFSLinkSelectorTest {

    private MapStatisticsCallerDao callerDao;
    private MapStatisticsCalleeDao calleeDao;
    private HostApplicationMapDao hostApplicationMapDao;

    Application APP_A = new Application("APP_A", ServiceType.STAND_ALONE);
    Application APP_B = new Application("APP_B", ServiceType.STAND_ALONE);
    Application APP_C = new Application("APP_B", ServiceType.STAND_ALONE);


    Range range = new Range(0, 100);
    SearchOption option = new SearchOption(1, 1);


    @Before
    public void setUp() throws Exception {
        this.callerDao = mock(MapStatisticsCallerDao.class);
        this.calleeDao = mock(MapStatisticsCalleeDao.class);
        this.hostApplicationMapDao = mock(HostApplicationMapDao.class);

    }

    @Test
    public void testEmpty() throws Exception {

        when(callerDao.selectCaller((Application) anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());
        when(calleeDao.selectCallee((Application) anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName((Application) anyObject(), (Range) anyObject())).thenReturn(new HashSet<AcceptApplication>());

        BFSLinkSelector bfsLinkSelector = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);


        LinkDataDuplexMap select = bfsLinkSelector.select(APP_A, range, option);

        Assert.assertEquals(select.size(), 0);


    }

    @Test
    public void testCaller() throws Exception {
        // APP_A -> APP_B
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, HistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), 1);

        when(callerDao.selectCaller(eq(APP_A), (Range) anyObject())).thenReturn(linkDataMap);
        when(calleeDao.selectCallee((Application) anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName((Application) anyObject(), (Range) anyObject())).thenReturn(new HashSet<AcceptApplication>());

        BFSLinkSelector bfsLinkSelector = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);


        Range range = new Range(0, 100);
        SearchOption option = new SearchOption(1, 1);
        LinkDataDuplexMap select = bfsLinkSelector.select(APP_A, range, option);

        Assert.assertEquals(select.size(), 1);
        Assert.assertEquals(select.getSourceLinkDataList().size(), 1);
        Assert.assertEquals(select.getTargetLinkDataList().size(), 0);
    }

    @Test
    public void testCaller_tier_3() throws Exception {
        // APP_A -> APP_B -> APP_C

        LinkDataMap link_A_B = new LinkDataMap();
        link_A_B.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, HistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), 1);
        when(callerDao.selectCaller(eq(APP_A), (Range) anyObject())).thenReturn(link_A_B);

        LinkDataMap link_B_C = new LinkDataMap();
        link_B_C.addLinkData(APP_B, "agentB", APP_C, "agentC", 1000, HistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), 2);
        when(callerDao.selectCaller(eq(APP_B), (Range) anyObject())).thenReturn(link_B_C);

        when(calleeDao.selectCallee((Application) anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName((Application) anyObject(), (Range) anyObject())).thenReturn(new HashSet<AcceptApplication>());


        Range range = new Range(0, 100);
        // depth 1
        SearchOption option = new SearchOption(1, 1);
        BFSLinkSelector bfsLinkSelector = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);
        LinkDataDuplexMap select = bfsLinkSelector.select(APP_A, range, option);

        Assert.assertEquals(select.size(), 1);
        Assert.assertEquals(select.getSourceLinkDataList().size(), 1);
        Assert.assertEquals(select.getTargetLinkDataList().size(), 0);
        assertSource_Target_TotalCount("APP_A->APP_B", select, new LinkKey(APP_A, APP_B), 1);

        // depth 2
        SearchOption depth2 = new SearchOption(2, 2);
        BFSLinkSelector bfsLinkSelector2 = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);
        LinkDataDuplexMap select_depth2 = bfsLinkSelector2.select(APP_A, range, depth2);
        Assert.assertEquals(select_depth2.size(), 2);

        LinkKey linkKey_A_B = new LinkKey(APP_A, APP_B);
        assertSource_Target_TotalCount("APP_A->APP_B", select_depth2, linkKey_A_B, 1);

        LinkKey linkKey_B_C = new LinkKey(APP_B, APP_C);
        assertSource_Target_TotalCount("APP_B->APP_C", select_depth2, linkKey_B_C, 2);
    }

    private void assertSource_Target_TotalCount(String message, LinkDataDuplexMap linkData, LinkKey linkKey, long count) {
        LinkData sourceLinkData = linkData.getSourceLinkData(linkKey);
        AgentHistogramList targetList = sourceLinkData.getTargetList();
        long totalCount = targetList.getTotalCount();
        Assert.assertEquals(message, totalCount, count);
    }

    @Test
    public void testCallee() throws Exception {
        // APP_A -> APP_B
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, HistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), 1);

        when(callerDao.selectCaller((Application) anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());
        when(calleeDao.selectCallee(eq(APP_B), (Range) anyObject())).thenReturn(linkDataMap);
        when(hostApplicationMapDao.findAcceptApplicationName((Application) anyObject(), (Range) anyObject())).thenReturn(new HashSet<AcceptApplication>());

        BFSLinkSelector bfsLinkSelector = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);


        Range range = new Range(0, 100);
        SearchOption option = new SearchOption(1, 1);
        LinkDataDuplexMap select = bfsLinkSelector.select(APP_B, range, option);

        Assert.assertEquals(select.size(), 1);
        Assert.assertEquals(select.getSourceLinkDataList().size(), 0);
        Assert.assertEquals(select.getTargetLinkDataList().size(), 1);

    }

    @Test
    public void testCallee_tier_3() throws Exception {
        // APP_A -> APP_B -> APP_C
        LinkDataMap linkDataMap_A_B = new LinkDataMap();
        linkDataMap_A_B.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, HistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), 1);
        when(calleeDao.selectCallee(eq(APP_B), (Range) anyObject())).thenReturn(linkDataMap_A_B);

        LinkDataMap linkDataMap_B_C = new LinkDataMap();
        linkDataMap_B_C.addLinkData(APP_B, "agentB", APP_C, "agentC", 1000, HistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), 2);
        when(calleeDao.selectCallee(eq(APP_C), (Range) anyObject())).thenReturn(linkDataMap_B_C);

        when(calleeDao.selectCallee((Application)anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());

        when(callerDao.selectCaller((Application) anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName((Application) anyObject(), (Range) anyObject())).thenReturn(new HashSet<AcceptApplication>());

        BFSLinkSelector bfsLinkSelector = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);


        Range range = new Range(0, 100);
        SearchOption option = new SearchOption(1, 1);
        LinkDataDuplexMap select = bfsLinkSelector.select(APP_B, range, option);

        Assert.assertEquals(select.size(), 1);
        Assert.assertEquals(select.getSourceLinkDataList().size(), 0);
        Assert.assertEquals(select.getTargetLinkDataList().size(), 1);


        // depth 2
        SearchOption depth2 = new SearchOption(2, 2);
        BFSLinkSelector bfsLinkSelector2 = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);
        LinkDataDuplexMap select_depth2 = bfsLinkSelector2.select(APP_A, range, depth2);
        Assert.assertEquals(select_depth2.size(), 2);

        LinkKey linkKey_A_B = new LinkKey(APP_A, APP_B);
        assertTarget_Source_TotalCount("APP_A->APP_B", select_depth2, linkKey_A_B, 1);

        LinkKey linkKey_B_C = new LinkKey(APP_B, APP_C);
        assertTarget_Source_TotalCount("APP_B->APP_C", select_depth2, linkKey_B_C, 2);

    }

    private void assertTarget_Source_TotalCount(String message, LinkDataDuplexMap linkData, LinkKey linkKey, long count) {
        LinkData sourceLinkData = linkData.getTargetLinkData(linkKey);
        AgentHistogramList targetList = sourceLinkData.getTargetList();
        long totalCount = targetList.getTotalCount();
        Assert.assertEquals(message, totalCount, count);
    }



}

