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

package com.navercorp.pinpoint.web.service.map.processor;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.service.map.processor.WasOnlyProcessor;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author HyunGil Jeong
 */
public class WasOnlyProcessorTest {

    private final Range testRange = new Range(System.currentTimeMillis(), System.currentTimeMillis());

    @Test
    public void shouldFilterLinksToTerminalNodes() {
        // Given
        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        ServiceType terminalServiceType = ServiceTypeFactory.of(2222, "TERMINAL", ServiceTypeProperty.TERMINAL);
        Application toApplication = new Application("TERMINAL", terminalServiceType);
        LinkData wasToTerminalLinkData = new LinkData(fromApplication, toApplication);
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(wasToTerminalLinkData);

        // When
        WasOnlyProcessor wasOnlyProcessor = new WasOnlyProcessor();
        LinkDataMap filteredLinkDataMap = wasOnlyProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        Assert.assertTrue(filteredLinkDataMap.getLinkDataList().isEmpty());
    }

    @Test
    public void shouldFilterLinksToUnknownNodes() {
        // Given
        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application("UNKNOWN", ServiceType.UNKNOWN);
        LinkData wasToUnknownLinkData = new LinkData(fromApplication, toApplication);
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(wasToUnknownLinkData);

        // When
        WasOnlyProcessor wasOnlyProcessor = new WasOnlyProcessor();
        LinkDataMap filteredLinkDataMap = wasOnlyProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        Assert.assertTrue(filteredLinkDataMap.getLinkDataList().isEmpty());
    }

    @Test
    public void shouldNotFilterLinksToWasNodes() {
        // Given
        Application fromApplication = new Application("WAS_FROM", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application("WAS_TO", ServiceType.TEST_STAND_ALONE);
        LinkData wasToWasLinkData = new LinkData(fromApplication, toApplication);
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(wasToWasLinkData);

        // When
        WasOnlyProcessor wasOnlyProcessor = new WasOnlyProcessor();
        LinkDataMap filteredLinkDataMap = wasOnlyProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        Assert.assertFalse(filteredLinkDataMap.getLinkDataList().isEmpty());
    }

    @Test
    public void shouldNotFilterLinksToQueueNodes() {
        // Given
        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application("QUEUE", ServiceTypeFactory.of(8888, "QUEUE", ServiceTypeProperty.QUEUE));
        LinkData wasToQueueLinkData = new LinkData(fromApplication, toApplication);
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(wasToQueueLinkData);

        // When
        WasOnlyProcessor wasOnlyProcessor = new WasOnlyProcessor();
        LinkDataMap filteredLinkDataMap = wasOnlyProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        Assert.assertFalse(filteredLinkDataMap.getLinkDataList().isEmpty());
    }
}
