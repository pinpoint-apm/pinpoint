/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.dao.hbase.stat.v1;

import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.dao.stat.TransactionDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV1;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Deprecated
@Repository("transactionDaoV1")
public class HbaseTransactionDao implements TransactionDao {

    @Autowired
    private AgentStatMapperV1.TransactionMapper mapper;

    @Autowired
    private Aggregator.TransactionAggregator aggregator;

    @Autowired
    private HbaseAgentStatDaoOperations operations;

    @Override
    public List<TransactionBo> getAgentStatList(String agentId, Range range) {
        return operations.getAgentStatList(mapper, aggregator, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        return operations.agentStatExists(mapper, agentId, range);
    }
}
