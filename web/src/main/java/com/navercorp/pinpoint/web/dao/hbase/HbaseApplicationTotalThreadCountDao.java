/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.bo.codec.stat.join.TotalThreadCountDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationTotalThreadCountDao;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class HbaseApplicationTotalThreadCountDao implements ApplicationTotalThreadCountDao {
    private final TotalThreadCountDecoder totalThreadCountDecoder;
    private final ApplicationStatSampler<JoinTotalThreadCountBo> totalThreadCountSampler;
    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationTotalThreadCountDao(TotalThreadCountDecoder totalThreadCountDecoder, ApplicationStatSampler<JoinTotalThreadCountBo> totalThreadCountSampler, HbaseApplicationStatDaoOperations operations) {
        this.totalThreadCountDecoder = Objects.requireNonNull(totalThreadCountDecoder, "totalThreadCountDecoder");
        this.totalThreadCountSampler = Objects.requireNonNull(totalThreadCountSampler, "totalThreadCountSampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinTotalThreadCountBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        ApplicationStatMapper mapper = operations.createRowMapper(totalThreadCountDecoder, range);
        SampledApplicationStatResultExtractor resultExtractor = new SampledApplicationStatResultExtractor(timeWindow, mapper, totalThreadCountSampler);
        List<AggregationStatData> aggregationStatDataList = operations.getSampledStatList(StatType.APP_TOTAL_THREAD_COUNT, resultExtractor, applicationId, range);
        return cast(aggregationStatDataList);
    }

    private List<AggreJoinTotalThreadCountBo> cast(List<AggregationStatData> aggregationStatDataList) {
        List<AggreJoinTotalThreadCountBo> aggreJoinTotalThreadCountBoList = new ArrayList<>(aggregationStatDataList.size());

        for (AggregationStatData aggregationStatData : aggregationStatDataList) {
            aggreJoinTotalThreadCountBoList.add((AggreJoinTotalThreadCountBo) aggregationStatData);
        }

        return aggreJoinTotalThreadCountBoList;
    }
}
