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

import com.navercorp.pinpoint.common.server.bo.codec.stat.join.TransactionDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationTransactionDao;
import com.navercorp.pinpoint.web.mapper.stat.ApplicationStatMapper;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseApplicationTransactionDao implements ApplicationTransactionDao {

    private final TransactionDecoder transactionDecoder;

    private final ApplicationStatSampler<JoinTransactionBo> transactionSampler;

    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationTransactionDao(TransactionDecoder transactionDecoder, ApplicationStatSampler<JoinTransactionBo> transactionSampler, HbaseApplicationStatDaoOperations operations) {
        this.transactionDecoder = transactionDecoder;
        this.transactionSampler = transactionSampler;
        this.operations = operations;
    }

    @Override
    public List<AggreJoinTransactionBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        ApplicationStatMapper mapper = operations.createRowMapper(transactionDecoder, range);
        SampledApplicationStatResultExtractor resultExtractor = new SampledApplicationStatResultExtractor(timeWindow, mapper, transactionSampler);
        List<AggregationStatData> aggregationStatDataList = operations.getSampledStatList(StatType.APP_TRANSACTION_COUNT, resultExtractor, applicationId, range);
        return cast(aggregationStatDataList);
    }

    private List<AggreJoinTransactionBo> cast(List<AggregationStatData> aggregationStatDataList) {
        List<AggreJoinTransactionBo> aggreJoinTransactionBoList = new ArrayList<>(aggregationStatDataList.size());

        for (AggregationStatData aggregationStatData : aggregationStatDataList) {
            aggreJoinTransactionBoList.add((AggreJoinTransactionBo) aggregationStatData);
        }

        return aggreJoinTransactionBoList;
    }
}
