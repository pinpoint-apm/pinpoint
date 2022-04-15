/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPointSummary;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class ResponseTimeSampler implements AgentStatSampler<ResponseTimeBo, SampledResponseTime> {

    @Override
    public SampledResponseTime sampleDataPoints(int timeWindowIndex, long timestamp, List<ResponseTimeBo> dataPoints, ResponseTimeBo previousDataPoint) {
        List<Long> avgs = getAvg(dataPoints);
        AgentStatPoint<Long> avg = createPoint(timestamp, avgs);

        List<Long> maxs = getMax(dataPoints);
        AgentStatPoint<Long> max = createPoint(timestamp, maxs);

        SampledResponseTime sampledResponseTime = new SampledResponseTime(avg, max);
        return sampledResponseTime;
    }

    private List<Long> getAvg(List<ResponseTimeBo> dataPoints) {
        List<Long> avgs = new ArrayList<>(dataPoints.size());
        for (ResponseTimeBo responseTimeBo : dataPoints) {
            avgs.add(responseTimeBo.getAvg());
        }
        return avgs;
    }

    private List<Long> getMax(List<ResponseTimeBo> dataPoints) {
        List<Long> maxs = new ArrayList<>(dataPoints.size());
        for (ResponseTimeBo responseTimeBo : dataPoints) {
            maxs.add(responseTimeBo.getMax());
        }
        return maxs;
    }

    private AgentStatPoint<Long> createPoint(long timestamp, List<Long> values) {
        if (CollectionUtils.isEmpty(values)) {
            return SampledResponseTime.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.longSummary(timestamp, values);
    }

}
