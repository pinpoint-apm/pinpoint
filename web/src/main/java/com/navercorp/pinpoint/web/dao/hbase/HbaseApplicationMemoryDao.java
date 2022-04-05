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

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationMemoryDao;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseApplicationMemoryDao implements ApplicationMemoryDao {

    private final ApplicationStatDecoder<JoinMemoryBo> memoryDecoder;

    private final ApplicationStatSampler<JoinMemoryBo, AggreJoinMemoryBo> memorySampler;

    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationMemoryDao(ApplicationStatDecoder<JoinMemoryBo> memoryDecoder,
                                     ApplicationStatSampler<JoinMemoryBo, AggreJoinMemoryBo> memorySampler,
                                     HbaseApplicationStatDaoOperations operations) {
        this.memoryDecoder = Objects.requireNonNull(memoryDecoder, "memoryDecoder");
        this.memorySampler = Objects.requireNonNull(memorySampler, "memorySampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinMemoryBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        RowMapper<List<JoinMemoryBo>> mapper = operations.createRowMapper(memoryDecoder, range);
        ResultsExtractor<List<AggreJoinMemoryBo>> resultExtractor = new SampledApplicationStatResultExtractor<>(timeWindow, mapper, memorySampler);
        return operations.getSampledStatList(StatType.APP_MEMORY_USED, resultExtractor, applicationId, range);
    }

}
