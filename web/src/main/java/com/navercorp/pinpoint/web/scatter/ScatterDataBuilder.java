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

import com.navercorp.pinpoint.web.scatter.vo.Dot;
import com.navercorp.pinpoint.web.scatter.vo.DotAgentInfo;
import org.eclipse.collections.api.factory.primitive.LongObjectMaps;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class ScatterDataBuilder {

    private final long from;
    private final long to;
    private final int xGroupUnitMillis;
    private final int yGroupUnitMillis;

    private ScatterAgentMetadataRepository scatterAgentMetadataRepository = new ScatterAgentMetadataRepository();
    private MutableLongObjectMap<DotGroups> scatterData;

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

        this.scatterData = LongObjectMaps.mutable.withInitialCapacity(16);
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

        final long acceptedTime = dot.getAcceptedTime();
        long acceptedTimeDiff = acceptedTime - from;
        long x = acceptedTimeDiff - (acceptedTimeDiff  % xGroupUnitMillis);
        if (x < 0) {
            x = 0L;
        }
        int y = dot.getElapsedTime() - (dot.getElapsedTime() % yGroupUnitMillis);

        Coordinates coordinates = new Coordinates(x, y);
        addDot(coordinates, new Dot(dot.getTransactionId(), acceptedTimeDiff, dot.getElapsedTime(), dot.getExceptionCode(), dot.getAgentId()));

        oldestAcceptedTime = Math.min(oldestAcceptedTime, acceptedTime);
        latestAcceptedTime = Math.max(latestAcceptedTime, acceptedTime);
    }

    private void addDot(Coordinates coordinates, Dot dot) {
        final long x = coordinates.x();
        DotGroups dotGroups = this.scatterData.getIfAbsentPut(x, () -> new DotGroups(x));

        dotGroups.addDot(coordinates, dot);

        scatterAgentMetadataRepository.addDotAgentInfo(new DotAgentInfo(dot));
    }


    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public ScatterData build() {
        MutableLongObjectMap<DotGroups> copyScatterData = this.scatterData;
        this.scatterData = LongObjectMaps.mutable.of();

        ScatterAgentMetadataRepository copyRepo = new ScatterAgentMetadataRepository(this.scatterAgentMetadataRepository.getDotAgentInfoSet());
        this.scatterAgentMetadataRepository = new ScatterAgentMetadataRepository();

        return new ScatterData(from, to, this.oldestAcceptedTime, this.latestAcceptedTime, copyScatterData, copyRepo);
    }

}
