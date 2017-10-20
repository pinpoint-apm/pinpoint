/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.service.map;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.hadoop.hbase.shaded.org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public abstract class BidirectionalLinkSelectorTestBase extends LinkSelectorTestBase {

    @Override
    protected LinkSelectorType getLinkSelectorType() {
        return LinkSelectorType.BIDIRECTIONAL;
    }

    @Test
    public void testBidirectionalGraph() {
        // APP_IN_IN -> APP_IN -> APP_A(selected) -> APP_OUT -> APP_OUT_OUT
        //                 |-> APP_IN_OUT   APP_OUT_IN -^
        final Application APP_A = new Application("APP_A", ServiceType.TEST_STAND_ALONE);

        final Application APP_IN = new Application("APP_IN", ServiceType.TEST_STAND_ALONE);
        final Application APP_IN_IN = new Application("APP_IN_IN", ServiceType.TEST_STAND_ALONE);
        final Application APP_IN_OUT = new Application("APP_IN_OUT", ServiceType.TEST_STAND_ALONE);

        final Application APP_OUT = new Application("APP_OUT", ServiceType.TEST_STAND_ALONE);
        final Application APP_OUT_OUT = new Application("APP_OUT_OUT", ServiceType.TEST_STAND_ALONE);
        final Application APP_OUT_IN = new Application("APP_OUT_IN", ServiceType.TEST_STAND_ALONE);

        int callCount = 10;

        LinkDataMap link_IN_to_A = new LinkDataMap();
        link_IN_to_A.addLinkData(APP_IN, "agentIn", APP_A, "agentA", 1000, ServiceType.STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount);
        LinkDataMap link_IN_IN_to_IN= new LinkDataMap();
        link_IN_IN_to_IN.addLinkData(APP_IN_IN, "agentInIn", APP_IN, "agentIn", 1000, ServiceType.STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount);
        LinkDataMap link_IN_to_IN_OUT = new LinkDataMap();
        link_IN_to_IN_OUT.addLinkData(APP_IN, "agentIn", APP_IN_OUT, "agentInOut", 1000, ServiceType.STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount);

        LinkDataMap link_A_to_OUT = new LinkDataMap();
        link_A_to_OUT.addLinkData(APP_A, "agentA", APP_OUT, "agentOut", 1000, ServiceType.STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount);
        LinkDataMap link_OUT_to_OUT_OUT = new LinkDataMap();
        link_OUT_to_OUT_OUT.addLinkData(APP_OUT, "agentOut", APP_OUT_OUT, "agentOutOut", 1000, ServiceType.STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount);
        LinkDataMap link_OUT_IN_to_OUT = new LinkDataMap();
        link_OUT_IN_to_OUT.addLinkData(APP_OUT_IN, "agentOutIn", APP_OUT, "agentOut", 1000, ServiceType.STAND_ALONE.getHistogramSchema().getNormalSlot().getSlotTime(), callCount);

        when(linkDataMapService.selectCallerLinkDataMap(any(Application.class), any(Range.class))).thenAnswer(new Answer<LinkDataMap>() {
            @Override
            public LinkDataMap answer(InvocationOnMock invocation) throws Throwable {
                Application callerApplication = invocation.getArgument(0);
                if (callerApplication.equals(APP_A)) {
                    return link_A_to_OUT;
                } else if (callerApplication.equals(APP_IN)) {
                    LinkDataMap linkDataMap = new LinkDataMap();
                    linkDataMap.addLinkDataMap(link_IN_to_A);
                    linkDataMap.addLinkDataMap(link_IN_to_IN_OUT);
                    return linkDataMap;
                } else if (callerApplication.equals(APP_IN_IN)) {
                    return link_IN_IN_to_IN;
                } else if (callerApplication.equals(APP_OUT)) {
                    return link_OUT_to_OUT_OUT;
                } else if (callerApplication.equals(APP_OUT_IN)) {
                    return link_OUT_IN_to_OUT;
                }
                return newEmptyLinkDataMap();
            }
        });
        when(linkDataMapService.selectCalleeLinkDataMap(any(Application.class), any(Range.class))).thenAnswer(new Answer<LinkDataMap>() {
            @Override
            public LinkDataMap answer(InvocationOnMock invocation) throws Throwable {
                Application calleeApplication = invocation.getArgument(0);
                if (calleeApplication.equals(APP_A)) {
                    return link_IN_to_A;
                } else if (calleeApplication.equals(APP_IN)) {
                    return link_IN_IN_to_IN;
                } else if (calleeApplication.equals(APP_IN_OUT)) {
                    return link_IN_to_IN_OUT;
                } else if (calleeApplication.equals(APP_OUT)) {
                    LinkDataMap linkDataMap = new LinkDataMap();
                    linkDataMap.addLinkDataMap(link_A_to_OUT);
                    linkDataMap.addLinkDataMap(link_OUT_IN_to_OUT);
                    return linkDataMap;
                } else if (calleeApplication.equals(APP_OUT_OUT)) {
                    return link_OUT_to_OUT_OUT;
                }
                return newEmptyLinkDataMap();
            }
        });
        when(hostApplicationMapDao.findAcceptApplicationName(any(Application.class), any(Range.class))).thenReturn(new HashSet<>());

        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(getLinkSelectorType());
        LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(Collections.singletonList(APP_A), range, 2, 2);

        // APP_IN_IN -> APP_IN (callee)
        LinkKey linkKey_IN_IN_to_IN = new LinkKey(APP_IN_IN, APP_IN);
        LinkData linkData_IN_IN_to_IN = linkDataDuplexMap.getTargetLinkData(linkKey_IN_IN_to_IN);
        Assert.assertNotNull(linkData_IN_IN_to_IN);
        Assert.assertEquals(callCount, linkData_IN_IN_to_IN.getTotalCount());
        // APP_IN -> APP_A (callee)
        LinkKey linkKey_IN_to_A = new LinkKey(APP_IN, APP_A);
        LinkData linkData_IN_to_A = linkDataDuplexMap.getTargetLinkData(linkKey_IN_to_A);
        Assert.assertNotNull(linkData_IN_to_A);
        Assert.assertEquals(callCount, linkData_IN_to_A.getTotalCount());
        // APP_IN (caller) -> APP_IN_OUT
        LinkKey linkKey_IN_to_IN_OUT = new LinkKey(APP_IN, APP_IN_OUT);
        LinkData linkData_IN_to_IN_OUT = linkDataDuplexMap.getSourceLinkData(linkKey_IN_to_IN_OUT);
        Assert.assertNotNull(linkData_IN_to_IN_OUT);
        Assert.assertEquals(callCount, linkData_IN_to_IN_OUT.getTotalCount());

        // APP_A (caller) -> APP_OUT
        LinkKey linkKey_A_to_OUT = new LinkKey(APP_A, APP_OUT);
        LinkData linkData_A_to_OUT = linkDataDuplexMap.getSourceLinkData(linkKey_A_to_OUT);
        Assert.assertNotNull(linkData_A_to_OUT);
        Assert.assertEquals(callCount, linkData_A_to_OUT.getTotalCount());
        // APP_OUT (caller) -> APP_OUT_OUT
        LinkKey linkKey_OUT_to_OUT_OUT = new LinkKey(APP_OUT, APP_OUT_OUT);
        LinkData linkData_OUT_to_OUT_OUT = linkDataDuplexMap.getSourceLinkData(linkKey_OUT_to_OUT_OUT);
        Assert.assertNotNull(linkData_OUT_to_OUT_OUT);
        Assert.assertEquals(callCount, linkData_OUT_to_OUT_OUT.getTotalCount());
        // APP_OUT_IN -> APP_OUT (callee)
        LinkKey linkKey_OUT_IN_to_OUT = new LinkKey(APP_OUT_IN, APP_OUT);
        LinkData linkData_OUT_IN_to_OUT = linkDataDuplexMap.getTargetLinkData(linkKey_OUT_IN_to_OUT);
        Assert.assertNotNull(linkData_OUT_IN_to_OUT);
        Assert.assertEquals(callCount, linkData_OUT_IN_to_OUT.getTotalCount());
    }
}
