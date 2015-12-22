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

package com.navercorp.pinpoint.web.applicationmap.rawdata;

import java.util.*;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;

public class LinkDataMap {
    private final Map<LinkKey, LinkData> linkDataMap = new HashMap<>();
    private TimeWindow timeWindow;

    public LinkDataMap() {
        this(null);
    }

    public LinkDataMap(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }


    public Collection<LinkData> getLinkDataList() {
        return linkDataMap.values();
    }

    public void addLinkData(Application sourceApplication, String sourceAgentId, Application destinationApplication, String destinationAgentId, long timestamp, short slotTime, long count) {
        final LinkData linkData = getLinkData(sourceApplication, destinationApplication);
        linkData.addLinkData(sourceAgentId, sourceApplication.getServiceType(), destinationAgentId, destinationApplication.getServiceType(), timestamp, slotTime, count);
    }

    @Override
    public String toString() {
        return "LinkDataMap [" + linkDataMap + "]";
    }

    public void addLinkDataMap(LinkDataMap linkDataMap) {
        if (linkDataMap == null) {
            throw new NullPointerException("linkDataMap must not be null");
        }
        for (LinkData copyLinkData : linkDataMap.linkDataMap.values()) {
            addLinkData(copyLinkData);
        }
    }

    public void addLinkData(LinkData copyLinkData) {
        if (copyLinkData == null) {
            throw new NullPointerException("copyLinkData must not be null");
        }
        Application fromApplication = copyLinkData.getFromApplication();
        Application toApplication = copyLinkData.getToApplication();
        LinkData linkData = getLinkData(fromApplication, toApplication);
        linkData.add(copyLinkData);
    }

    private LinkData getLinkData(Application fromApplication, Application toApplication) {
        final LinkKey key = new LinkKey(fromApplication, toApplication);
        LinkData findLink = linkDataMap.get(key);
        if (findLink == null) {
            findLink = new LinkData(fromApplication, toApplication, timeWindow);
            linkDataMap.put(key, findLink);
        }
        return findLink;
    }

    // test api
    public long getTotalCount() {
        long totalCount = 0;
        for (LinkData linkData : linkDataMap.values()) {
            totalCount += linkData.getTotalCount();
        }
        return totalCount;
    }

    public int size() {
        return linkDataMap.size();
    }

    public LinkData getLinkData(LinkKey findLinkKey) {
        if (findLinkKey == null) {
            throw new NullPointerException("findLinkKey must not be null");
        }
        return this.linkDataMap.get(findLinkKey);
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }
}