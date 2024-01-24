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
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author HyunGil Jeong
 */
public class ApplicationLimiterProcessor implements LinkDataMapProcessor {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int EMPTY = 0;

    private final int maxSize;

    private static final AtomicIntegerFieldUpdater<ApplicationLimiterProcessor> counterUpdater =
            AtomicIntegerFieldUpdater.newUpdater(ApplicationLimiterProcessor.class, "counter");

    private volatile int counter = 0;


    public ApplicationLimiterProcessor(int maxSize) {
        this.maxSize = maxSize;
    }

    boolean limitReached() {
        return maxSize < counter;
    }


    @Override
    public LinkDataMap processLinkDataMap(LinkDirection direction, LinkDataMap linkDataMap, Range range) {
        final int linkSize = linkDataMap.size();
        final int remain = remain(linkSize);
        if (remain == EMPTY) {
            return new LinkDataMap();
        }
        if (remain >= linkSize) {
            // bypass
            return linkDataMap;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} Remain {}-->{}, link count {}", direction, linkDataMap, linkSize, remain);
        }
        Collection<LinkData> linkDataList = linkDataMap.getLinkDataList();

        LinkDataMap copy = new LinkDataMap(linkDataMap.getTimeWindow());
        int i = 0;
        for (LinkData linkData : linkDataList) {
            logger.debug("add link {}", linkData);
            copy.addLinkData(linkData);
            if (++i >= remain) {
                logger.debug("break {}/{}", i, remain);
                break;
            }
        }
        return copy;
    }

    int remain(int demand) {
        // max : 10
        // current 8, demand : 5 -> 2
        // current 8, demand : 1 -> 1
        final int increment = counterUpdater.addAndGet(this, demand);
        int before = increment - demand;
        int remain = maxSize - before;
        if (remain < 0) {
            return EMPTY;
        }
        return Math.min(remain, demand);
    }

}
