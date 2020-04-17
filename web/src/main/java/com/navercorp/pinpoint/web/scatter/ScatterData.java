/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.scatter;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.ScatterDataSerializer;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotAgentInfo;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = ScatterDataSerializer.class)
public class ScatterData {

    private final long from;
    private final long to;

    private final ScatterAgentMetadataRepository scatterAgentMetadataRepository;
    private final Map<Long, DotGroups> scatterData;

    private final long oldestAcceptedTime;
    private final long latestAcceptedTime;

    public ScatterData(long from,
                       long to,
                       long oldestAcceptedTime,
                       long latestAcceptedTime,
                       Map<Long, DotGroups> scatterData,
                       ScatterAgentMetadataRepository scatterAgentMetadataRepository) {
        if (from <= 0) {
            throw new IllegalArgumentException("from value must be higher than 0");
        }
        if (from > to) {
            throw new IllegalArgumentException("from value must be lower or equal to to value");
        }

        this.from = from;
        this.to = to;
        this.oldestAcceptedTime = oldestAcceptedTime;
        this.latestAcceptedTime = latestAcceptedTime;
        this.scatterData = Objects.requireNonNull(scatterData, "scatterData");
        this.scatterAgentMetadataRepository = Objects.requireNonNull(scatterAgentMetadataRepository, "scatterAgentMetadataRepository");
    }

    ScatterAgentMetadataRepository getScatterAgentMetadataRepository() {
        return scatterAgentMetadataRepository;
    }

    public ScatterAgentMetaData getScatterAgentMetadata() {
        return new ScatterAgentMetaData(scatterAgentMetadataRepository.getDotAgentInfoSet());
    }

    public Map<Long, DotGroups> getScatterDataMap() {
        return scatterData;
    }

    public Map<Long, DotGroups> getSortedScatterDataMap() {
        TreeMap<Long, DotGroups> sortedMap = new TreeMap<>(new XCoordinatesComparator());
        sortedMap.putAll(scatterData);

        return sortedMap;
    }

    public int getDotSize() {
        int totalDotSize = 0;

        for (DotGroups dotGroups : scatterData.values()) {
            Collection<DotGroup> dotGroupList = dotGroups.getDotGroupMap().values();
            for (DotGroup dotGroup : dotGroupList) {
                totalDotSize += dotGroup.getDotSize();
            }
        }
        return totalDotSize;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public long getOldestAcceptedTime() {
        if (oldestAcceptedTime == Long.MAX_VALUE) {
            return -1;
        }
        return oldestAcceptedTime;
    }

    public long getLatestAcceptedTime() {
        if (latestAcceptedTime == Long.MIN_VALUE) {
            return -1;
        }
        return latestAcceptedTime;
    }

    private static class XCoordinatesComparator implements Comparator<Long> {

        @Override
        public int compare(Long o1, Long o2) {
            return Long.compare(o2, o1);
        }

    }

}
