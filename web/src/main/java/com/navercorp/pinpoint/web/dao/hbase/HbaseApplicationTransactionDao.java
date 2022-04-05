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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.ApplicationTransactionDao;
import com.navercorp.pinpoint.web.mapper.stat.SampledApplicationStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseApplicationTransactionDao implements ApplicationTransactionDao {

    private final ApplicationStatDecoder<JoinTransactionBo> transactionDecoder;

    private final ApplicationStatSampler<JoinTransactionBo, AggreJoinTransactionBo> transactionSampler;

    private final HbaseApplicationStatDaoOperations operations;

    public HbaseApplicationTransactionDao(ApplicationStatDecoder<JoinTransactionBo> transactionDecoder,
                                          ApplicationStatSampler<JoinTransactionBo, AggreJoinTransactionBo> transactionSampler,
                                          HbaseApplicationStatDaoOperations operations) {
        this.transactionDecoder = transactionDecoder;
        this.transactionSampler = transactionSampler;
        this.operations = operations;
    }

    @Override
    public List<AggreJoinTransactionBo> getApplicationStatList(String applicationId, TimeWindow timeWindow) {
        Range range = timeWindow.getWindowSlotRange();

        RowMapper<List<JoinTransactionBo>> mapper = operations.createRowMapper(transactionDecoder, range);
        ResultsExtractor<List<AggreJoinTransactionBo>> resultExtractor = new SampledApplicationStatResultExtractor<>(timeWindow, mapper, transactionSampler);
        return operations.getSampledStatList(StatType.APP_TRANSACTION_COUNT, resultExtractor, applicationId, range);
    }

}
