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
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class ResponseTimeSampler implements AgentStatSampler<ResponseTimeBo, SampledResponseTime> {

    public static final DownSampler<Long> LONG_DOWN_SAMPLER = DownSamplers.getLongDownSampler(ResponseTimeBo.UNCOLLECTED_VALUE);

    @Override
    public SampledResponseTime sampleDataPoints(int timeWindowIndex, long timestamp, List<ResponseTimeBo> dataPoints, ResponseTimeBo previousDataPoint) {
        List<Long> avgs = new ArrayList<>(dataPoints.size());
        for (ResponseTimeBo responseTimeBo : dataPoints) {
            avgs.add(responseTimeBo.getAvg());
        }

        SampledResponseTime sampledResponseTime = new SampledResponseTime();
        sampledResponseTime.setAvg(createPoint(timestamp, avgs));
        return sampledResponseTime;
    }

    private Point<Long, Long> createPoint(long timestamp, List<Long> values) {
        if (values.isEmpty()) {
            return new UncollectedPoint<>(timestamp, ResponseTimeBo.UNCOLLECTED_VALUE);
        } else {
            return new Point<>(
                    timestamp,
                    LONG_DOWN_SAMPLER.sampleMin(values),
                    LONG_DOWN_SAMPLER.sampleMax(values),
                    LONG_DOWN_SAMPLER.sampleAvg(values),
                    LONG_DOWN_SAMPLER.sampleSum(values));
        }
    }

}
