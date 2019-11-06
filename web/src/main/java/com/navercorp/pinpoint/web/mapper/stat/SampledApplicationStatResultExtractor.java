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
package com.navercorp.pinpoint.web.mapper.stat;

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSamplingHandler;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.join.EagerSamplingHandler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.util.List;

/**
 * @author minwoo.jung
 */
public class SampledApplicationStatResultExtractor implements ResultsExtractor<List<AggregationStatData>> {

    private final TimeWindow timeWindow;
    private final ApplicationStatMapper rowMapper;
    private final ApplicationStatSampler sampler;

    public SampledApplicationStatResultExtractor(TimeWindow timeWindow, ApplicationStatMapper rowMapper, ApplicationStatSampler sampler) {
        if (timeWindow.getWindowRangeCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range yields too many timeslots");
        }
        this.timeWindow = timeWindow;
        this.rowMapper = rowMapper;
        this.sampler = sampler;
    }

    @Override
    public List<AggregationStatData> extractData(ResultScanner results) throws Exception {
        int rowNum = 0;
        ApplicationStatSamplingHandler samplingHandler = new EagerSamplingHandler(timeWindow, sampler);
        for (Result result : results) {
            for (JoinStatBo dataPoint : this.rowMapper.mapRow(result, rowNum++)) {
                samplingHandler.addDataPoint(dataPoint);
            }
        }
        return samplingHandler.getSampledDataPoints();
    }
}
