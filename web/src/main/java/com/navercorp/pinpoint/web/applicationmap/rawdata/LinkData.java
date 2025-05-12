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


import com.navercorp.pinpoint.common.timeseries.window.TimeWindowFunction;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * application Out/In relationship stored in DB
 *
 * @author netspider
 * @author emeroad
 */
public class LinkData {

    private final Application fromApplication;
    private final Application toApplication;

    private LinkCallDataMap linkCallDataMap;
    private final TimeWindowFunction timeWindow;

    public static LinkData copyOf(Application fromApplication, Application toApplication, LinkCallDataMap linkCallDataMap) {
        Objects.requireNonNull(linkCallDataMap, "linkCallDataMap");
        return new LinkData(fromApplication, toApplication, TimeWindowFunction.identity(), linkCallDataMap);
    }

    public LinkData(Application fromApplication, Application toApplication) {
        this(fromApplication, toApplication, TimeWindowFunction.identity());
    }

    public LinkData(Application fromApplication, Application toApplication, TimeWindowFunction timeWindow) {
        this.fromApplication = Objects.requireNonNull(fromApplication, "fromApplication");
        this.toApplication = Objects.requireNonNull(toApplication, "toApplication");

        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.linkCallDataMap = new LinkCallDataMap(timeWindow);
    }

    private LinkData(Application fromApplication, Application toApplication, TimeWindowFunction timeWindow, LinkCallDataMap linkCallDataMap) {
        this.fromApplication = Objects.requireNonNull(fromApplication, "fromApplication");
        this.toApplication = Objects.requireNonNull(toApplication, "toApplication");

        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.linkCallDataMap = Objects.requireNonNull(linkCallDataMap, "linkCallDataMap");
    }

    /**
     *
     * @param hostname
     *            host name or endpoint
     * @param slot
     * @param count
     */
    public void addLinkData(String outAgentId, ServiceType outServiceTypeCode, String hostname, ServiceType serviceTypeCode, long timestamp, short slot, long count) {
        Objects.requireNonNull(hostname, "hostname");

        this.linkCallDataMap.addCallData(outAgentId, outServiceTypeCode, hostname, serviceTypeCode, timestamp, slot, count);
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

    public LinkCallDataMap getLinkCallDataMap() {
        return  this.linkCallDataMap;
    }

    public AgentHistogramList getTargetList() {
        return linkCallDataMap.getOutLinkList();
    }

    public AgentHistogramList getSourceList() {
        return linkCallDataMap.getInLinkList();
    }

    public void add(final LinkData linkData) {
        Objects.requireNonNull(linkData, "linkData");

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

    public int size() {
        return this.linkCallDataMap.size();
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
