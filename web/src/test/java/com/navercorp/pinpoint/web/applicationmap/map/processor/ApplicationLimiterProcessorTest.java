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

package com.navercorp.pinpoint.web.applicationmap.map.processor;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApplicationLimiterProcessorTest {

    Application application1 = new Application("test1", ServiceType.TEST);
    Application application2 = new Application("test2", ServiceType.TEST);
    Range between = Range.between(10, 100);

    @Test
    void limitReached() {

        ApplicationLimiterProcessor processor = new ApplicationLimiterProcessor(2);
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(application1, application1));

        processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap, between);
        Assertions.assertFalse(processor.limitReached());

        processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap, between);
        Assertions.assertFalse(processor.limitReached());


        processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap, between);
        Assertions.assertTrue(processor.limitReached());
    }


    @Test
    void limitReached_2() {

        ApplicationLimiterProcessor processor = new ApplicationLimiterProcessor(2);
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(application1, application1));

        processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap, between);
        Assertions.assertFalse(processor.limitReached());

        LinkDataMap linkDataMap2 = new LinkDataMap();
        linkDataMap2.addLinkData(new LinkData(application1, application1));
        linkDataMap2.addLinkData(new LinkData(application2, application2));
        LinkDataMap replaceLink = processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap2, between);
        Assertions.assertTrue(processor.limitReached());
        Assertions.assertEquals(1, replaceLink.size());

        processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap, between);
        Assertions.assertTrue(processor.limitReached());
    }

    @Test
    void limitReached_bypass() {

        ApplicationLimiterProcessor processor = new ApplicationLimiterProcessor(2);
        LinkDataMap linkDataMap = new LinkDataMap();
        linkDataMap.addLinkData(new LinkData(application1, application1));

        LinkDataMap result = processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap, between);
        Assertions.assertSame(linkDataMap, result);

        linkDataMap.addLinkData(new LinkData(application2, application2));
        LinkDataMap result2 = processor.processLinkDataMap(LinkDirection.IN_LINK, linkDataMap, between);
        Assertions.assertNotSame(linkDataMap, result2);
    }


    @Test
    void remain() {

        ApplicationLimiterProcessor processor = new ApplicationLimiterProcessor(10);

        // 4
        int remain1 = processor.remain(4);
        Assertions.assertEquals(4, remain1);

        // 6
        int remain2 = processor.remain(2);
        Assertions.assertEquals(2, remain2);

        // 11
        int remain3 = processor.remain(5);
        Assertions.assertEquals(4, remain3);

        // 16
        int remain4 = processor.remain(5);
        Assertions.assertEquals(0, remain4);

    }

    @Test
    void remain2() {

        ApplicationLimiterProcessor processor = new ApplicationLimiterProcessor(10);

        int remain1 = processor.remain(100);
        Assertions.assertEquals(10, remain1);
    }

}