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
package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.server.bo.codec.stat.join.MemoryDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationMemoryDao;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseApplicationMemoryDao implements ApplicationMemoryDao {

    private final MemoryDecoder memoryDecoder;

    private final ApplicationStatSampler<JoinMemoryBo> memorySampler;

    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationMemoryDao(MemoryDecoder memoryDecoder, ApplicationStatSampler<JoinMemoryBo> memorySampler, HbaseApplicationStatDaoOperations operations) {
        this.memoryDecoder = Objects.requireNonNull(memoryDecoder, "memoryDecoder");
        this.memorySampler = Objects.requireNonNull(memorySampler, "memorySampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinMemoryBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        ApplicationStatMapper mapper = operations.createRowMapper(memoryDecoder, range);
        SampledApplicationStatResultExtractor resultExtractor = new SampledApplicationStatResultExtractor(timeWindow, mapper, memorySampler);
        List<AggregationStatData> aggregationStatDataList = operations.getSampledStatList(StatType.APP_MEMORY_USED, resultExtractor, applicationId, range);
        return cast(aggregationStatDataList);
    }

    private List<AggreJoinMemoryBo> cast(List<AggregationStatData> aggregationStatDataList) {
        List<AggreJoinMemoryBo> aggreJoinMemoryBoList = new ArrayList<>(aggregationStatDataList.size());

        for (AggregationStatData aggregationStatData : aggregationStatDataList) {
            aggreJoinMemoryBoList.add((AggreJoinMemoryBo) aggregationStatData);
        }

        return aggreJoinMemoryBoList;
    }
}
