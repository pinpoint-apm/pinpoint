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
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCalleeDao;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;

import static org.mockito.Matchers.anyObject;
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
    // APP_A ->

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

        Assert.assertEquals(select.size(), 1);


    }

    @Test
    public void testCaller() throws Exception {
        Application sourceApp = new Application("APP_A", ServiceType.STAND_ALONE);
        Application targetApp = new Application("APP_B", ServiceType.STAND_ALONE);

        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(sourceApp, "agentA", targetApp, "agentB", 1000, HistogramSchema.FAST_SCHEMA.getNormalSlot().getSlotTime(), 1);
        when(callerDao.selectCaller((Application) anyObject(), (Range) anyObject())).thenReturn(linkDataMap);
        when(calleeDao.selectCallee((Application) anyObject(), (Range) anyObject())).thenReturn(new LinkDataMap());
        when(hostApplicationMapDao.findAcceptApplicationName((Application) anyObject(), (Range) anyObject())).thenReturn(new HashSet<AcceptApplication>());

        BFSLinkSelector bfsLinkSelector = new BFSLinkSelector(this.callerDao, this.calleeDao, hostApplicationMapDao);


        Range range = new Range(0, 100);
        SearchOption option = new SearchOption(1, 1);
        LinkDataDuplexMap select = bfsLinkSelector.select(sourceApp, range, option);

        Assert.assertEquals(select.size(), 1);
        Assert.assertEquals(select.getSourceLinkDataList().size(), 1);

    }



}

