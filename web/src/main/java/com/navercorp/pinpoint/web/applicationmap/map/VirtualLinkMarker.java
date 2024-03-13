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

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author HyunGil Jeong
 */
public class VirtualLinkMarker {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Set<LinkData> virtualLinkDataMarker = ConcurrentHashMap.newKeySet();

    public Set<LinkData> getVirtualLinkData() {
        return Collections.unmodifiableSet(virtualLinkDataMarker);
    }

    public List<LinkData> createVirtualLink(LinkData linkData, Application toApplication, Set<AcceptApplication> acceptApplicationList) {
        logger.info("one to N replaced. node:{}->host:{} accept:{}", linkData.getFromApplication(), toApplication.name(), acceptApplicationList);
        List<LinkData> virtualLinkDataList = new ArrayList<>();
        logger.debug("acceptApplicationList:{}", acceptApplicationList);
        for (AcceptApplication acceptApplication : acceptApplicationList) {
            // linkCallData needs to be modified - remove callHistogram on purpose
            LinkData virtualLinkData = new LinkData(linkData.getFromApplication(), acceptApplication.getApplication());
            virtualLinkData.setLinkCallDataMap(linkData.getLinkCallDataMap());
            virtualLinkDataList.add(virtualLinkData);
            markVirtualLink(virtualLinkData);
        }
        return virtualLinkDataList;
    }

    private void markVirtualLink(LinkData virtualLinkData) {
        final boolean add = virtualLinkDataMarker.add(virtualLinkData);
        if (!add) {
            logger.warn("virtualLinkData add error - {}", virtualLinkData);
        }
    }
}
