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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.web.view.ScatterDataSerializer;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotAgentInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
@JsonSerialize(using = ScatterDataSerializer.class)
public class ScatterDataBuilder {

    private final long from;
    private final long to;
    private final int xGroupUnitMillis;
    private final int yGroupUnitMillis;

    private final ScatterAgentMetadataRepository scatterAgentMetadataRepository = new ScatterAgentMetadataRepository();
    private final Map<Long, DotGroups> scatterData = new HashMap<>();

    private long oldestAcceptedTime = Long.MAX_VALUE;
    private long latestAcceptedTime = Long.MIN_VALUE;

    public ScatterDataBuilder(long from, long to, int xGroupUnitMillis, int yGroupUnitMillis) {
        if (from <= 0) {
            throw new IllegalArgumentException("from value must be higher than 0");
        }
        if (from > to) {
            throw new IllegalArgumentException("from value must be lower or equal to to value");
        }

        this.from = from;
        this.to = to;
        this.xGroupUnitMillis = xGroupUnitMillis;
        this.yGroupUnitMillis = yGroupUnitMillis;
    }

    public void addDot(List<Dot> dotList) {
        for (Dot dot : dotList) {
            addDot(dot);
        }
    }

    public void addDot(Dot dot) {
        if (dot == null) {
            return;
        }

        long acceptedTimeDiff = dot.getAcceptedTime() - from;
        long x = acceptedTimeDiff - (acceptedTimeDiff  % xGroupUnitMillis);
        if (x < 0) {
            x = 0L;
        }
        int y = dot.getElapsedTime() - (dot.getElapsedTime() % yGroupUnitMillis);

        Coordinates coordinates = new Coordinates(x, y);
        addDot(coordinates, new Dot(dot.getTransactionId(), acceptedTimeDiff, dot.getElapsedTime(), dot.getExceptionCode(), dot.getAgentId()));

        oldestAcceptedTime = Math.min(oldestAcceptedTime, dot.getAcceptedTime());
        latestAcceptedTime = Math.max(latestAcceptedTime, dot.getAcceptedTime());
    }

    private void addDot(Coordinates coordinates, Dot dot) {
        final Long x = coordinates.getX();
        DotGroups dotGroups = this.scatterData.get(x);
        if (dotGroups == null) {
            dotGroups = new DotGroups(x);
            this.scatterData.put(x, dotGroups);
        }

        dotGroups.addDot(coordinates, dot);

        scatterAgentMetadataRepository.addDotAgentInfo(new DotAgentInfo(dot));
    }

    public void merge(ScatterData scatterData) {
        if (scatterData == null) {
            return;
        }

        final Map<Long, DotGroups> scatterDataMap = scatterData.getScatterDataMap();
        for (Map.Entry<Long, DotGroups> entry : scatterDataMap.entrySet()) {
            final Long key = entry.getKey();

            final DotGroups dotGroups = this.scatterData.get(key);
            if (dotGroups == null) {
                this.scatterData.put(key, entry.getValue());
            } else {
                dotGroups.merge(entry.getValue());
            }
        }

        scatterAgentMetadataRepository.merge(scatterData.getScatterAgentMetadataRepository());


        oldestAcceptedTime = Math.min(oldestAcceptedTime, scatterData.getOldestAcceptedTime());
        latestAcceptedTime = Math.max(latestAcceptedTime, scatterData.getLatestAcceptedTime());
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public ScatterData build() {
        Map<Long, DotGroups> copyScatterData = new HashMap<>(this.scatterData);
        Set<DotAgentInfo> dotAgentInfoSet = new HashSet<>(this.scatterAgentMetadataRepository.getDotAgentInfoSet());
        ScatterAgentMetadataRepository copyRepo = new ScatterAgentMetadataRepository(dotAgentInfoSet);
        return new ScatterData(from, to, this.oldestAcceptedTime, this.latestAcceptedTime, copyScatterData, copyRepo);
    }

}
