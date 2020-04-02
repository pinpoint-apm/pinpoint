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

import com.navercorp.pinpoint.common.server.bo.codec.stat.join.DataSourceDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationDataSourceDao;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceListBo;
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
public class HbaseApplicationDataSourceDao implements ApplicationDataSourceDao {

    private final DataSourceDecoder dataSourceDecoder;

    private final ApplicationStatSampler<JoinDataSourceListBo> dataSourceSampler;

    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationDataSourceDao(DataSourceDecoder dataSourceDecoder,
                                         ApplicationStatSampler<JoinDataSourceListBo> dataSourceSampler, HbaseApplicationStatDaoOperations operations) {
        this.dataSourceDecoder = Objects.requireNonNull(dataSourceDecoder, "dataSourceDecoder");
        this.dataSourceSampler = Objects.requireNonNull(dataSourceSampler, "dataSourceSampler");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    @Override
    public List<AggreJoinDataSourceListBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = new Range(scanFrom, scanTo);
        ApplicationStatMapper mapper = operations.createRowMapper(dataSourceDecoder, range);
        SampledApplicationStatResultExtractor resultExtractor = new SampledApplicationStatResultExtractor(timeWindow, mapper, dataSourceSampler);
        List<AggregationStatData> aggregationStatDataList = operations.getSampledStatList(StatType.APP_DATA_SOURCE, resultExtractor, applicationId, range);
        return cast(aggregationStatDataList);
    }

    private List<AggreJoinDataSourceListBo> cast(List<AggregationStatData> aggregationStatDataList) {
        List<AggreJoinDataSourceListBo> aggreJoinDataSourceListBoList = new ArrayList<>(aggregationStatDataList.size());

        for (AggregationStatData aggregationStatData : aggregationStatDataList) {
            aggreJoinDataSourceListBoList.add((AggreJoinDataSourceListBo) aggregationStatData);
        }

        return aggreJoinDataSourceListBoList;
    }
}
