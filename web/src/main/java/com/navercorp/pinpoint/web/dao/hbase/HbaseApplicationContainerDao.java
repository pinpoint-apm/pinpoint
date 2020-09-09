/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.bo.codec.stat.join.ContainerDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinContainerBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationContainerDao;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinContainerBo;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class HbaseApplicationContainerDao implements ApplicationContainerDao {

    private final ContainerDecoder containerDecoder;
    private final ApplicationStatSampler<JoinContainerBo> containerSampler;
    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationContainerDao(ContainerDecoder containerDecoder, ApplicationStatSampler<JoinContainerBo> containerSampler, HbaseApplicationStatDaoOperations operations) {
        this.containerDecoder = Objects.requireNonNull(containerDecoder, "containerDecoder");
        this.containerSampler = Objects.requireNonNull(containerSampler, "containerSampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinContainerBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = Range.newRange(scanFrom, scanTo);
        ApplicationStatMapper mapper = operations.createRowMapper(containerDecoder, range);
        SampledApplicationStatResultExtractor resultExtractor = new SampledApplicationStatResultExtractor(timeWindow, mapper, containerSampler);
        List<AggregationStatData> aggregationStatDataList = operations.getSampledStatList(StatType.APP_CONTAINER, resultExtractor, applicationId, range);
        return cast(aggregationStatDataList);
    }

    private List<AggreJoinContainerBo> cast(List<AggregationStatData> aggregationStatDataList) {
        List<AggreJoinContainerBo> aggreJoinContainerBoList = new ArrayList<>(aggregationStatDataList.size());

        for (AggregationStatData aggregationStatData : aggregationStatDataList) {
            aggreJoinContainerBoList.add((AggreJoinContainerBo) aggregationStatData);
        }

        return aggreJoinContainerBoList;
    }
}
