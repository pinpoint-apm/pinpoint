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

import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowFunction;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LinkDataMap {
    private final Map<LinkKey, LinkData> linkDataMap = new HashMap<>();
    private final TimeWindowFunction timeWindow;

    public LinkDataMap() {
        this(TimeWindowFunction.identity());
    }

    public LinkDataMap(TimeWindowFunction timeWindow) {
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
    }


    public Collection<LinkData> getLinkDataList() {
        return linkDataMap.values();
    }

    public void addLinkData(Application sourceApplication, String sourceAgentId, Application destinationApplication, String destinationAgentId, long timestamp, short slotTime, long count) {
        final LinkData linkData = getLinkData(sourceApplication, destinationApplication);
        linkData.addLinkData(sourceAgentId, sourceApplication.serviceType(), destinationAgentId, destinationApplication.serviceType(), timestamp, slotTime, count);
    }

    @Override
    public String toString() {
        return "LinkDataMap [" + linkDataMap + "]";
    }

    public void addLinkDataMap(LinkDataMap linkDataMap) {
        Objects.requireNonNull(linkDataMap, "linkDataMap");

        for (LinkData copyLinkData : linkDataMap.linkDataMap.values()) {
            addLinkData(copyLinkData);
        }
    }

    public void addLinkData(LinkData copyLinkData) {
        Objects.requireNonNull(copyLinkData, "copyLinkData");

        Application fromApplication = copyLinkData.getFromApplication();
        Application toApplication = copyLinkData.getToApplication();
        LinkData linkData = getLinkData(fromApplication, toApplication);
        linkData.add(copyLinkData);
    }

    private LinkData getLinkData(Application fromApplication, Application toApplication) {
        final LinkKey key = new LinkKey(fromApplication, toApplication);
        return linkDataMap.computeIfAbsent(key, k -> new LinkData(fromApplication, toApplication, timeWindow));
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
        Objects.requireNonNull(findLinkKey, "findLinkKey");

        return this.linkDataMap.get(findLinkKey);
    }

    public TimeWindowFunction getTimeWindow() {
        return timeWindow;
    }

}