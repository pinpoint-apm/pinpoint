/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.mapper.stat.sampling.AgentStatSamplingHandler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.EagerSamplingHandler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.util.List;


/**
 * @author HyunGil Jeong
 */
public class SampledAgentStatResultExtractor<T extends AgentStatDataPoint, S extends SampledAgentStatDataPoint> implements ResultsExtractor<List<S>> {

    private final TimeWindow timeWindow;
    private final AgentStatMapper<T> rowMapper;
    private final AgentStatSampler<T, S> sampler;

    public SampledAgentStatResultExtractor(TimeWindow timeWindow, AgentStatMapper<T> rowMapper, AgentStatSampler<T, S> sampler) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        this.rowMapper = rowMapper;
        this.sampler = sampler;
    }

    @Override
    public List<S> extractData(ResultScanner results) throws Exception {
        int rowNum = 0;
        AgentStatSamplingHandler<T, S> samplingHandler = new EagerSamplingHandler<>(timeWindow, sampler);
        for (Result result : results) {
            for (T dataPoint : this.rowMapper.mapRow(result, rowNum++)) {
                samplingHandler.addDataPoint(dataPoint);
            }
        }
        return samplingHandler.getSampledDataPoints();
    }
}
