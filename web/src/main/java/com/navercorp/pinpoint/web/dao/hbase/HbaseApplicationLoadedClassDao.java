/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.hbase.ResultsExtractor;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationLoadedClassDao;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HbaseApplicationLoadedClassDao implements ApplicationLoadedClassDao {

    private final ApplicationStatDecoder<JoinLoadedClassBo> loadedClassDecoder;
    private final ApplicationStatSampler<JoinLoadedClassBo, AggreJoinLoadedClassBo> loadedClassSampler;
    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationLoadedClassDao(ApplicationStatDecoder<JoinLoadedClassBo> loadedClassDecoder,
                                          ApplicationStatSampler<JoinLoadedClassBo, AggreJoinLoadedClassBo> loadedClassSampler,
                                          HbaseApplicationStatDaoOperations operations) {
        this.loadedClassDecoder = Objects.requireNonNull(loadedClassDecoder, "directBufferDecoder");
        this.loadedClassSampler = Objects.requireNonNull(loadedClassSampler, "directBufferSampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinLoadedClassBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        RowMapper<List<JoinLoadedClassBo>> mapper = operations.createRowMapper(loadedClassDecoder, range);
        ResultsExtractor<List<AggreJoinLoadedClassBo>> resultExtractor = new SampledApplicationStatResultExtractor<>(timeWindow, mapper, loadedClassSampler);
        return operations.getSampledStatList(StatType.APP_LOADED_CLASS, resultExtractor, applicationId, range);
    }

}
