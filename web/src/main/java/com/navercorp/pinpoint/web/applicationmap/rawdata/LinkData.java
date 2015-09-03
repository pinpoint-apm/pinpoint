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
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * application caller/callee relationship stored in DB
 *
 * @author netspider
 * @author emeroad
 */
public class LinkData {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application fromApplication;
    private final Application toApplication;

    private LinkCallDataMap linkCallDataMap;
    private final TimeWindow timeWindow;

    public LinkData(Application fromApplication, Application toApplication) {
        this(fromApplication, toApplication, null);
    }
    public LinkData(Application fromApplication, Application toApplication, TimeWindow timeWindow) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }

        this.fromApplication = fromApplication;
        this.toApplication = toApplication;

        this.timeWindow = timeWindow;
        this.linkCallDataMap = new LinkCallDataMap(timeWindow);
    }

    /**
     *
     * @param hostname
     *            host name or endpoint
     * @param slot
     * @param count
     */
    public void addLinkData(String callerAgentId, ServiceType callerServiceTypeCode, String hostname, ServiceType serviceTypeCode, long timestamp, short slot, long count) {
        if (hostname == null) {
            throw new NullPointerException("hostname must not be null");
        }
        this.linkCallDataMap.addCallData(callerAgentId, callerServiceTypeCode, hostname, serviceTypeCode, timestamp, slot, count);
    }

    public void resetLinkData() {
        this.linkCallDataMap = new LinkCallDataMap(timeWindow);
    }


    public Application getFromApplication() {
        return this.fromApplication;
    }

    public Application getToApplication() {
        return this.toApplication;
    }

    public void setLinkCallDataMap(LinkCallDataMap linkCallDataMap) {
        this.linkCallDataMap = linkCallDataMap;
    }

    public LinkCallDataMap getLinkCallDataMap() {
        return  this.linkCallDataMap;
    }

    public AgentHistogramList getTargetList() {
        return linkCallDataMap.getTargetList();
    }

    public AgentHistogramList getSourceList() {
        return linkCallDataMap.getSourceList();
    }

    public void add(final LinkData linkData) {
        if (linkData == null) {
            throw new NullPointerException("linkData must not be null");
        }
        if (!this.equals(linkData)) {
            throw new IllegalArgumentException("Can't merge with different link.");
        }
        final LinkCallDataMap target = linkData.linkCallDataMap;
        this.linkCallDataMap.addLinkDataMap(target);
    }

    // test api
    public long getTotalCount() {
        long totalCount = 0;
        for (LinkCallData linkCallData : linkCallDataMap.getLinkDataList()) {
            totalCount += linkCallData.getTotalCount();
        }
        return totalCount;
    }

    @Override
    public String toString() {
        return "LinkData{" +
                "fromApplication=" + fromApplication +
                ", toApplication=" + toApplication +
                ", " + linkCallDataMap +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkData that = (LinkData) o;

        if (!fromApplication.equals(that.fromApplication)) return false;
        if (!toApplication.equals(that.toApplication)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fromApplication.hashCode();
        result = 31 * result + toApplication.hashCode();
        return result;
    }
}
