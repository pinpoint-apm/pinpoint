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

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.TransactionDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.dao.stat.TransactionDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Repository("transactionDaoV2")
public class HbaseTransactionDaoV2 implements TransactionDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final TransactionDecoder transactionDecoder;

    public HbaseTransactionDaoV2(HbaseAgentStatDaoOperationsV2 operations, TransactionDecoder transactionDecoder) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.transactionDecoder = Objects.requireNonNull(transactionDecoder, "transactionDecoder");
    }

    @Override
    public List<TransactionBo> getAgentStatList(String agentId, Range range) {
        AgentStatMapperV2<TransactionBo> mapper = operations.createRowMapper(transactionDecoder, range);
        return operations.getAgentStatList(AgentStatType.TRANSACTION, mapper, agentId, range);
    }

    @Override
    public boolean agentStatExists(String agentId, Range range) {
        AgentStatMapperV2<TransactionBo> mapper = operations.createRowMapper(transactionDecoder, range);
        return operations.agentStatExists(AgentStatType.TRANSACTION, mapper, agentId, range);
    }
}
