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

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
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

import static org.mockito.Matchers.any;
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

    private Application APP_A = new Application("APP_A", ServiceType.STAND_ALONE);
    private Application APP_B = new Application("APP_B", ServiceType.STAND_ALONE);
    private Application APP_C = new Application("APP_C", ServiceType.STAND_ALONE);


    private Range range = new Range(0, 100);

    private SearchOption oneDepth = new SearchOption(1, 1);
    private SearchOption twoDepth = new SearchOption(2, 2);


    @Before
    public void setUp() throws Exception {
        this.callerDao = mock(MapStatisticsCallerDao.class);
        this.calleeDao = mock(MapStatisticsCalleeDao.class);
        this.hostApplicationMapDao = mock(HostApplicationMapDao.class);
    }

    private LinkSelector createLinkSelector() {
        return new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao, null);
    }

    public LinkDataMap newEmptyLinkDataMap() {
        return new LinkDataMap();
    }

    @Test
    public void testEmpty() throws Exception {

        when(callerDao.selectCaller(any(Application.class), any(Range.class))).thenReturn(newEmptyLinkDataMap());
        when(calleeDao.selectCallee(any(Application.class), any(Range.class))).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(new HashSet<AcceptApplication>());

        LinkSelector linkSelector = createLinkSelector();
        LinkDataDuplexMap select = linkSelector.select(APP_A, range, oneDepth);

        Assert.assertEquals(select.size(), 0);
        Assert.assertEquals(select.getTotalCount(), 0);


    }

    @Test
    public void testCaller() throws Exception {
        // APP_A -> APP_B
        int callCount_A_B = 10;
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, BaseHistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), callCount_A_B);

        when(callerDao.selectCaller(eq(APP_A), any(Range.class))).thenReturn(linkDataMap);
        when(calleeDao.selectCallee(any(Application.class), any(Range.class))).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(new HashSet<AcceptApplication>());

        LinkSelector linkSelector = createLinkSelector();
        LinkDataDuplexMap linkData = linkSelector.select(APP_A, range, oneDepth);

        Assert.assertEquals(linkData.size(), 1);
        Assert.assertEquals(linkData.getTotalCount(), callCount_A_B);

        Assert.assertEquals(linkData.getSourceLinkDataList().size(), 1);
        Assert.assertEquals(linkData.getSourceLinkDataMap().getTotalCount(), callCount_A_B);

        Assert.assertEquals(linkData.getTargetLinkDataList().size(), 0);
    }

    @Test
    public void testCaller_3tier() throws Exception {
        // APP_A -> APP_B -> APP_C

        int callCount_A_B = 10;
        LinkDataMap link_A_B = new LinkDataMap();
        link_A_B.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, BaseHistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), callCount_A_B);
        when(callerDao.selectCaller(eq(APP_A), any(Range.class))).thenReturn(link_A_B);

        LinkDataMap link_B_C = new LinkDataMap();
        int callCount_B_C = 20;
        link_B_C.addLinkData(APP_B, "agentB", APP_C, "agentC", 1000, BaseHistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), callCount_B_C);
        when(callerDao.selectCaller(eq(APP_B), any(Range.class))).thenReturn(link_B_C);

        when(calleeDao.selectCallee(any(Application.class), any(Range.class))).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(new HashSet<AcceptApplication>());


        // depth 1
        LinkSelector linkSelector = createLinkSelector();
        LinkDataDuplexMap linkData = linkSelector.select(APP_A, range, oneDepth);

        Assert.assertEquals(linkData.size(), 1);
        Assert.assertEquals(linkData.getTotalCount(), callCount_A_B);

        Assert.assertEquals(linkData.getSourceLinkDataList().size(), 1);
        Assert.assertEquals(linkData.getSourceLinkDataMap().getTotalCount(), callCount_A_B);
        assertSource_Target_TotalCount("APP_A->APP_B", linkData, new LinkKey(APP_A, APP_B), callCount_A_B);

        Assert.assertEquals(linkData.getTargetLinkDataList().size(), 0);

        // depth 2
        LinkSelector linkSelector2 = createLinkSelector();
        LinkDataDuplexMap linkData_depth2 = linkSelector2.select(APP_A, range, twoDepth);
        Assert.assertEquals(linkData_depth2.size(), 2);
        Assert.assertEquals(linkData_depth2.getTotalCount(), callCount_A_B + callCount_B_C);

        LinkKey linkKey_A_B = new LinkKey(APP_A, APP_B);
        assertSource_Target_TotalCount("APP_A->APP_B", linkData_depth2, linkKey_A_B, callCount_A_B);

        LinkKey linkKey_B_C = new LinkKey(APP_B, APP_C);
        assertSource_Target_TotalCount("APP_B->APP_C", linkData_depth2, linkKey_B_C, callCount_B_C);
    }

    private void assertSource_Target_TotalCount(String message, LinkDataDuplexMap linkData, LinkKey linkKey, long count) {
        LinkData sourceLinkData = linkData.getSourceLinkData(linkKey);
        long totalCount = sourceLinkData.getTotalCount();
        Assert.assertEquals(message, totalCount, count);
    }

    @Test
    public void testCallee() throws Exception {
        // APP_A -> APP_B
        int callCount_A_B = 10;
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, BaseHistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), callCount_A_B);

        when(callerDao.selectCaller(any(Application.class), any(Range.class))).thenReturn(newEmptyLinkDataMap());
        when(calleeDao.selectCallee(eq(APP_B), any(Range.class))).thenReturn(linkDataMap);
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(new HashSet<AcceptApplication>());

        LinkSelector linkSelector = createLinkSelector();
        LinkDataDuplexMap linkData = linkSelector.select(APP_B, range, oneDepth);

        Assert.assertEquals(linkData.size(), 1);
        Assert.assertEquals(linkData.getTotalCount(), callCount_A_B);

        Assert.assertEquals(linkData.getSourceLinkDataList().size(), 0);

        Assert.assertEquals(linkData.getTargetLinkDataList().size(), 1);
        Assert.assertEquals(linkData.getTargetLinkDataMap().getTotalCount(), callCount_A_B);

    }



    @Test
    public void testCallee_3tier() throws Exception {
        // APP_A -> APP_B -> APP_C
        when(calleeDao.selectCallee(any(Application.class), any(Range.class))).thenReturn(newEmptyLinkDataMap());

        int callCount_A_B = 30;
        LinkDataMap linkDataMap_A_B = new LinkDataMap();
        linkDataMap_A_B.addLinkData(APP_A, "agentA", APP_B, "agentB", 1000, BaseHistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), callCount_A_B);
        when(calleeDao.selectCallee(eq(APP_B), any(Range.class))).thenReturn(linkDataMap_A_B);

        int callCount_B_C = 40;
        LinkDataMap linkDataMap_B_C = new LinkDataMap();
        linkDataMap_B_C.addLinkData(APP_B, "agentB", APP_C, "agentC", 1000, BaseHistogramSchema.NORMAL_SCHEMA.getNormalSlot().getSlotTime(), callCount_B_C);
        when(calleeDao.selectCallee(eq(APP_C), any(Range.class))).thenReturn(linkDataMap_B_C);

        when(callerDao.selectCaller(any(Application.class), any(Range.class))).thenReturn(newEmptyLinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(new HashSet<AcceptApplication>());


        LinkSelector linkSelector = createLinkSelector();
        LinkDataDuplexMap linkData = linkSelector.select(APP_C, range, oneDepth);

        Assert.assertEquals(linkData.size(), 1);
        Assert.assertEquals(linkData.getTotalCount(), callCount_B_C);

        Assert.assertEquals(linkData.getSourceLinkDataList().size(), 0);

        Assert.assertEquals(linkData.getTargetLinkDataList().size(), 1);
        Assert.assertEquals(linkData.getTotalCount(), callCount_B_C);


        // depth 2
        LinkSelector linkSelector2 = createLinkSelector();
        LinkDataDuplexMap linkData_depth2 = linkSelector2.select(APP_C, range, twoDepth);
        Assert.assertEquals(linkData_depth2.size(), 2);

        LinkKey linkKey_A_B = new LinkKey(APP_A, APP_B);
        assertTarget_Source_TotalCount("APP_A->APP_B", linkData_depth2, linkKey_A_B, callCount_A_B);

        LinkKey linkKey_B_C = new LinkKey(APP_B, APP_C);
        assertTarget_Source_TotalCount("APP_B->APP_C", linkData_depth2, linkKey_B_C, callCount_B_C);

    }

    private void assertTarget_Source_TotalCount(String message, LinkDataDuplexMap linkData, LinkKey linkKey, long count) {
        LinkData sourceLinkData = linkData.getTargetLinkData(linkKey);
        long totalCount = sourceLinkData.getTotalCount();
        Assert.assertEquals(message, totalCount, count);
    }



}

