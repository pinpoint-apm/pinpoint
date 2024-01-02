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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class LinkCallDataMap {

//    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Map<LinkKey, LinkCallData> linkDataMap = new HashMap<>();
    private final TimeWindow timeWindow;

    public LinkCallDataMap() {
        this(null);
    }

    public LinkCallDataMap(TimeWindow timeWindow) {
        this.timeWindow = timeWindow;
    }
    
    public TimeWindow getTimeWindow() {
        return this.timeWindow;
    }

    public void addCallData(Application sourceAgent, Application targetAgent, Collection<TimeHistogram> timeHistogramList) {
        LinkKey linkKey = new LinkKey(sourceAgent, targetAgent);
        LinkCallData linkCallData = getLinkCallData(linkKey);
        linkCallData.addCallData(timeHistogramList);
    }


    public void addCallData(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType, long timestamp, short slot, long count) {
        LinkKey linkKey = createLinkKey(sourceAgentId, sourceServiceType, targetId, targetServiceType);
        LinkCallData linkCallData = getLinkCallData(linkKey);
        linkCallData.addCallData(timestamp, slot, count);
    }

    public void addCallData(Application sourceAgent, Application targetAgent, long timestamp, short slot, long count) {
        LinkKey linkKey = new LinkKey(sourceAgent, targetAgent);
        LinkCallData linkCallData = getLinkCallData(linkKey);
        linkCallData.addCallData(timestamp, slot, count);
    }


    private LinkKey createLinkKey(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType) {
        return LinkKey.of(sourceAgentId, sourceServiceType, targetId, targetServiceType);
    }

    public void addLinkDataMap(LinkCallDataMap target) {
        Objects.requireNonNull(target, "target");

        for (Map.Entry<LinkKey, LinkCallData> copyEntry : target.linkDataMap.entrySet()) {
            final LinkKey key = copyEntry.getKey();
            final LinkCallData copyLinkCallData = copyEntry.getValue();
            LinkCallData linkCallData = getLinkCallData(key);
            linkCallData.addRawCallData(copyLinkCallData);
        }

    }

    private LinkCallData getLinkCallData(LinkKey key) {
        final Map<LinkKey, LinkCallData> rawCallDataMap = this.linkDataMap;
        LinkCallData linkCallData = rawCallDataMap.get(key);
        if (linkCallData == null) {
            linkCallData = new LinkCallData(key, timeWindow);
            rawCallDataMap.put(key, linkCallData);
        }
        return linkCallData;
    }

    public Collection<LinkCallData> getLinkDataList() {
        return linkDataMap.values();
    }

    public AgentHistogramList getOutLinkList() {
        AgentHistogramList targetList = new AgentHistogramList();
        for (Map.Entry<LinkKey, LinkCallData> linkKeyRawCallDataEntry : linkDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final LinkCallData linkCallData = linkKeyRawCallDataEntry.getValue();
            targetList.addTimeHistogram(key.getTo(), linkCallData.getTimeHistogram());
        }
        return targetList;
    }


    public AgentHistogramList getInLinkList() {
        AgentHistogramList sourceList = new AgentHistogramList();
        for (Map.Entry<LinkKey, LinkCallData> linkKeyRawCallDataEntry : linkDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final LinkCallData linkCallData = linkKeyRawCallDataEntry.getValue();
            // need target (to) ServiceType
            // the definition of source is data from the source when the source sends a request to a target.
            // Thus ServiceType is the target's ServiceType
            sourceList.addAgentHistogram(key.getFrom().getName(), key.getTo().getServiceType(), linkCallData.getTimeHistogram());
        }
        return sourceList;
    }

    @Override
    public String toString() {
        return "LinkCallDataMap{"
                    + linkDataMap +
                '}';
    }
}
