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

import com.google.common.collect.Sets;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.HostApplicationMapDao;
import com.navercorp.pinpoint.web.service.map.AcceptApplication;
import com.navercorp.pinpoint.web.service.map.VirtualLinkMarker;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Set;

import static com.navercorp.pinpoint.common.trace.ServiceTypeFactory.of;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.ALIAS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.TERMINAL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class RpcCallProcessorTest {

    private final Range testRange = Range.between(System.currentTimeMillis(), System.currentTimeMillis());

    @Mock
    private HostApplicationMapDao hostApplicationMapDao;

    @Test
    public void nonRpcClientOrQueueCallsShouldNotBeReplaced() {
        // Given
        ServiceType nonRpcClientOrQueueServiceType = mock(ServiceType.class);
        when(nonRpcClientOrQueueServiceType.isRpcClient()).thenReturn(false);
        when(nonRpcClientOrQueueServiceType.isQueue()).thenReturn(false);

        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application("NON_RPC_OR_QUEUE", nonRpcClientOrQueueServiceType);

        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(fromApplication, toApplication));

        // When
        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        RpcCallProcessor rpcCallProcessor = new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker);
        LinkDataMap replacedLinkDataMap = rpcCallProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        LinkKey linkKey = new LinkKey(fromApplication, toApplication);
        Assertions.assertNotNull(replacedLinkDataMap.getLinkData(linkKey));
        Assertions.assertEquals(linkDataMap.size(), replacedLinkDataMap.size());

        Assertions.assertTrue(virtualLinkMarker.getVirtualLinkData().isEmpty());
    }

    @Test
    public void oneAcceptApplication() {
        // Given
        ServiceType rpcClientServiceType = mock(ServiceType.class);
        when(rpcClientServiceType.isRpcClient()).thenReturn(true);
        String rpcUri = "accept.host/foo";

        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application(rpcUri, rpcClientServiceType);

        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(fromApplication, toApplication));

        Application expectedToApplication = new Application("ACCEPT_WAS", ServiceType.TEST_STAND_ALONE);
        when(hostApplicationMapDao.findAcceptApplicationName(fromApplication, testRange))
                .thenReturn(Sets.newHashSet(new AcceptApplication(rpcUri, expectedToApplication)));

        // When
        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        RpcCallProcessor rpcCallProcessor = new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker);
        LinkDataMap replacedLinkDataMap = rpcCallProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        LinkKey originalLinkKey = new LinkKey(fromApplication, toApplication);
        Assertions.assertNull(replacedLinkDataMap.getLinkData(originalLinkKey));

        LinkKey replacedLinkKey = new LinkKey(fromApplication, expectedToApplication);
        LinkData replacedLinkData = replacedLinkDataMap.getLinkData(replacedLinkKey);
        Assertions.assertNotNull(replacedLinkData);
        Assertions.assertEquals(fromApplication, replacedLinkData.getFromApplication());
        Assertions.assertEquals(expectedToApplication, replacedLinkData.getToApplication());

        Assertions.assertTrue(virtualLinkMarker.getVirtualLinkData().isEmpty());
    }

    @Test
    public void multipleAcceptApplications() {
        // Given
        ServiceType rpcClientServiceType = mock(ServiceType.class);
        when(rpcClientServiceType.isRpcClient()).thenReturn(true);
        String rpcUri = "accept.host/foo";

        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application(rpcUri, rpcClientServiceType);

        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(fromApplication, toApplication));

        Application expectedToApplication1 = new Application("ACCEPT_WAS1", ServiceType.TEST_STAND_ALONE);
        Application expectedToApplication2 = new Application("ACCEPT_WAS2", ServiceType.TEST_STAND_ALONE);
        when(hostApplicationMapDao.findAcceptApplicationName(fromApplication, testRange))
                .thenReturn(Sets.newHashSet(
                        new AcceptApplication(rpcUri, expectedToApplication1),
                        new AcceptApplication(rpcUri, expectedToApplication2)));

        // When
        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        RpcCallProcessor rpcCallProcessor = new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker);
        LinkDataMap replacedLinkDataMap = rpcCallProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        LinkKey originalLinkKey = new LinkKey(fromApplication, toApplication);
        Assertions.assertNull(replacedLinkDataMap.getLinkData(originalLinkKey));

        LinkKey replacedLinkKey1 = new LinkKey(fromApplication, expectedToApplication1);
        LinkData replacedLinkData1 = replacedLinkDataMap.getLinkData(replacedLinkKey1);
        Assertions.assertNotNull(replacedLinkData1);
        Assertions.assertEquals(fromApplication, replacedLinkData1.getFromApplication());
        Assertions.assertEquals(expectedToApplication1, replacedLinkData1.getToApplication());

        LinkKey replacedLinkKey2 = new LinkKey(fromApplication, expectedToApplication2);
        LinkData replacedLinkData2 = replacedLinkDataMap.getLinkData(replacedLinkKey2);
        Assertions.assertNotNull(replacedLinkData2);
        Assertions.assertEquals(fromApplication, replacedLinkData2.getFromApplication());
        Assertions.assertEquals(expectedToApplication2, replacedLinkData2.getToApplication());

        Set<LinkData> virtualLinkDatas = virtualLinkMarker.getVirtualLinkData();
        Assertions.assertTrue(virtualLinkDatas.contains(replacedLinkData1));
        Assertions.assertTrue(virtualLinkDatas.contains(replacedLinkData2));
    }

    @Test
    public void rpcWithoutAcceptApplication_shouldBeReplacedToUnknown() {
        // Given
        ServiceType rpcClientServiceType = mock(ServiceType.class);
        when(rpcClientServiceType.isRpcClient()).thenReturn(true);
        String rpcUri = "accept.host/foo";

        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application(rpcUri, rpcClientServiceType);
        Application expectedToApplication = new Application(rpcUri, ServiceType.UNKNOWN);

        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(fromApplication, toApplication));

        when(hostApplicationMapDao.findAcceptApplicationName(fromApplication, testRange)).thenReturn(Collections.emptySet());

        // When
        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        RpcCallProcessor rpcCallProcessor = new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker);
        LinkDataMap replacedLinkDataMap = rpcCallProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        LinkKey originalLinkKey = new LinkKey(fromApplication, toApplication);
        Assertions.assertNull(replacedLinkDataMap.getLinkData(originalLinkKey));

        LinkKey replacedLinkKey = new LinkKey(fromApplication, expectedToApplication);
        LinkData replacedLinkData = replacedLinkDataMap.getLinkData(replacedLinkKey);
        Assertions.assertEquals(fromApplication, replacedLinkData.getFromApplication());
        Assertions.assertEquals(expectedToApplication, replacedLinkData.getToApplication());

        Assertions.assertTrue(virtualLinkMarker.getVirtualLinkData().isEmpty());
    }

    @Test
    public void queueWithoutAcceptApplication_shouldNotReplace() {
        // Given
        ServiceType queueServiceType = mock(ServiceType.class);
        when(queueServiceType.isRpcClient()).thenReturn(false);
        when(queueServiceType.isQueue()).thenReturn(true);
        String queueName = "TestQueue";

        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application(queueName, queueServiceType);

        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(fromApplication, toApplication));

        when(hostApplicationMapDao.findAcceptApplicationName(fromApplication, testRange)).thenReturn(Collections.emptySet());

        // When
        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        RpcCallProcessor rpcCallProcessor = new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker);
        LinkDataMap replacedLinkDataMap = rpcCallProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        LinkKey linkKey = new LinkKey(fromApplication, toApplication);
        LinkData linkData = replacedLinkDataMap.getLinkData(linkKey);
        Assertions.assertEquals(fromApplication, linkData.getFromApplication());
        Assertions.assertEquals(toApplication, linkData.getToApplication());

        Assertions.assertTrue(virtualLinkMarker.getVirtualLinkData().isEmpty());
    }

    @Test
    public void multipleAcceptApplications_with_AliasAndOriginal() {

        ServiceType AliasServiceType = of(1008, "TEST_ALIAS_CLIENT", ALIAS);
        ServiceType ServerServiceType = of(1009, "TEST_ALIAS_SERVER", RECORD_STATISTICS, TERMINAL);

        // Given
        ServiceType rpcClientServiceType = mock(ServiceType.class);
        when(rpcClientServiceType.isRpcClient()).thenReturn(true);
        String rpcUri = "accept.host/foo";

        Application fromApplication = new Application("WAS", ServiceType.TEST_STAND_ALONE);
        Application toApplication = new Application(rpcUri, rpcClientServiceType);

        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(fromApplication, toApplication));

        Application expectedToApplication1 = new Application("AliasClient", AliasServiceType);
        Application expectedToApplication2 = new Application("AliasServer", ServerServiceType);
        when(hostApplicationMapDao.findAcceptApplicationName(fromApplication, testRange))
                .thenReturn(Sets.newHashSet(
                        new AcceptApplication(rpcUri, expectedToApplication1),
                        new AcceptApplication(rpcUri, expectedToApplication2)));

        // When
        VirtualLinkMarker virtualLinkMarker = new VirtualLinkMarker();
        RpcCallProcessor rpcCallProcessor = new RpcCallProcessor(hostApplicationMapDao, virtualLinkMarker);
        LinkDataMap replacedLinkDataMap = rpcCallProcessor.processLinkDataMap(linkDataMap, testRange);

        // Then
        LinkKey originalLinkKey = new LinkKey(fromApplication, toApplication);
        Assertions.assertNull(replacedLinkDataMap.getLinkData(originalLinkKey));

        LinkKey replacedLinkKey2 = new LinkKey(fromApplication, expectedToApplication2);
        LinkData replacedLinkData2 = replacedLinkDataMap.getLinkData(replacedLinkKey2);
        Assertions.assertNotNull(replacedLinkData2);
        Assertions.assertEquals(fromApplication, replacedLinkData2.getFromApplication());
        Assertions.assertEquals(expectedToApplication2, replacedLinkData2.getToApplication());

    }
}
