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

import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * representation of out/in relationship
 * @author emeroad
 */
public class LinkCallData {

    private final Application source;

    private final Application target;

    private final Map<Long, TimeHistogram> targetHistogramTimeMap;
    private final TimeWindow timeWindow;

    public LinkCallData(LinkKey linkKey) {
        this(linkKey, null);
    }
    public LinkCallData(LinkKey linkKey, TimeWindow timeWindow) {
        Objects.requireNonNull(linkKey, "linkKey");

        this.source = linkKey.getFrom();
        this.target = linkKey.getTo();

        this.targetHistogramTimeMap = new HashMap<>();
        this.timeWindow = timeWindow;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public Application getSource() {
        return source;
    }

    public Application getTarget() {
        return target;
    }

    public Collection<TimeHistogram> getTimeHistogram() {
        return targetHistogramTimeMap.values();
    }

    public void addCallData(long timestamp, short slot, long count) {
        TimeHistogram histogram = getTimeHistogram(timestamp);
        histogram.addCallCount(slot, count);
    }

    public void addCallData(Collection<TimeHistogram> timeHistogramList) {
        for (TimeHistogram timeHistogram : timeHistogramList) {
            TimeHistogram histogram = getTimeHistogram(timeHistogram.getTimeStamp());
            histogram.add(timeHistogram);
        }
    }

    public void addRawCallData(LinkCallData copyLinkCallData) {
        Objects.requireNonNull(copyLinkCallData, "copyLinkCallData");

        if (!this.source.equals(copyLinkCallData.source)) {
            throw new IllegalArgumentException("source not equals");
        }
        if (!this.target.equals(copyLinkCallData.target)) {
            throw new IllegalArgumentException("target not equals");
        }

        for (Map.Entry<Long, TimeHistogram> copyEntry : copyLinkCallData.targetHistogramTimeMap.entrySet()) {
            final Long timeStamp = copyEntry.getKey();
            TimeHistogram histogram = getTimeHistogram(timeStamp);
            histogram.add(copyEntry.getValue());
        }
    }

    private TimeHistogram getTimeHistogram(Long timeStamp) {
        long key = timeWindow != null ? timeWindow.refineTimestamp(timeStamp) : timeStamp;
        TimeHistogram histogram = targetHistogramTimeMap.get(key);
        if (histogram == null) {
            histogram = new TimeHistogram(target.getServiceType(), key);
            targetHistogramTimeMap.put(key, histogram);
        }
        return histogram;
    }

    public long getTotalCount() {
        long totalCount = 0;
        for (TimeHistogram timeHistogram : targetHistogramTimeMap.values()) {
            totalCount += timeHistogram.getTotalCount();
        }
        return totalCount;
    }

    @Override
    public String toString() {
        return "LinkCallData{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
