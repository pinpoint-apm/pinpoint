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

import com.navercorp.pinpoint.common.server.bo.codec.stat.join.LoadedClassDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationLoadedClassDao;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class HbaseApplicationLoadedClassDao implements ApplicationLoadedClassDao {

    private final LoadedClassDecoder loadedClassDecoder;
    private final ApplicationStatSampler<JoinLoadedClassBo> loadedClassSampler;
    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationLoadedClassDao(LoadedClassDecoder loadedClassDecoder, ApplicationStatSampler<JoinLoadedClassBo> loadedClassSampler, HbaseApplicationStatDaoOperations operations) {
        this.loadedClassDecoder = Objects.requireNonNull(loadedClassDecoder, "directBufferDecoder");
        this.loadedClassSampler = Objects.requireNonNull(loadedClassSampler, "directBufferSampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinLoadedClassBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = Range.newRange(scanFrom, scanTo);
        ApplicationStatMapper mapper = operations.createRowMapper(loadedClassDecoder, range);
        SampledApplicationStatResultExtractor resultExtractor = new SampledApplicationStatResultExtractor(timeWindow, mapper, loadedClassSampler);
        List<AggregationStatData> aggregationStatDataList = operations.getSampledStatList(StatType.APP_LOADED_CLASS, resultExtractor, applicationId, range);
        return cast(aggregationStatDataList);
    }

    private List<AggreJoinLoadedClassBo> cast(List<AggregationStatData> aggregationStatDataList) {
        List<AggreJoinLoadedClassBo> aggreJoinLoadedClassBoList = new ArrayList<>(aggregationStatDataList.size());

        for (AggregationStatData aggregationStatData : aggregationStatDataList) {
            aggreJoinLoadedClassBoList.add((AggreJoinLoadedClassBo) aggregationStatData);
        }

        return aggreJoinLoadedClassBoList;
    }
}
