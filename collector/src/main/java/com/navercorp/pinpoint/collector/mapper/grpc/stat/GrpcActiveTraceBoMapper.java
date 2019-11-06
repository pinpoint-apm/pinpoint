/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.mapper.grpc.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.trace.PActiveTrace;
import com.navercorp.pinpoint.grpc.trace.PActiveTraceHistogram;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class GrpcActiveTraceBoMapper {

    public ActiveTraceBo map(final PActiveTrace activeTrace) {
        final PActiveTraceHistogram histogram = activeTrace.getHistogram();
        final ActiveTraceHistogram activeTraceHistogram = createActiveTraceCountMap(histogram.getActiveTraceCountList());
        final ActiveTraceBo activeTraceBo = new ActiveTraceBo();
        activeTraceBo.setVersion((short) histogram.getVersion());
        activeTraceBo.setHistogramSchemaType(histogram.getHistogramSchemaType());
        activeTraceBo.setActiveTraceHistogram(activeTraceHistogram);
        return activeTraceBo;
    }

    private ActiveTraceHistogram createActiveTraceCountMap(final List<Integer> activeTraceCounts) {
        if (CollectionUtils.isEmpty(activeTraceCounts)) {
            return ActiveTraceHistogram.UNCOLLECTED;
        }
        if (activeTraceCounts.size() != 4) {
            return ActiveTraceHistogram.UNCOLLECTED;
        }
        final int fast = activeTraceCounts.get(0);
        final int normal = activeTraceCounts.get(1);
        final int slow = activeTraceCounts.get(2);
        final int verySlow = activeTraceCounts.get(3);

        return new ActiveTraceHistogram(fast, normal, slow, verySlow);
    }
}