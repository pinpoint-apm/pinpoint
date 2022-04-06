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
package com.navercorp.pinpoint.web.dao.hbase.appmetric;

import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import com.navercorp.pinpoint.web.dao.appmetric.ApplicationActiveTraceDao;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import org.springframework.stereotype.Repository;

/**
 * @author minwoo.jung
 */
@Repository
public class HbaseApplicationActiveTraceDao
        extends DefaultApplicationMetricDao<JoinActiveTraceBo, AggreJoinActiveTraceBo>
        implements ApplicationActiveTraceDao {

    public HbaseApplicationActiveTraceDao(ApplicationStatDecoder<JoinActiveTraceBo> decoder,
                                          ApplicationStatSampler<JoinActiveTraceBo, AggreJoinActiveTraceBo> sampler,
                                          HbaseApplicationStatDaoOperations operations) {
        super(StatType.APP_ACTIVE_TRACE_COUNT, decoder, sampler, operations);
    }

}
