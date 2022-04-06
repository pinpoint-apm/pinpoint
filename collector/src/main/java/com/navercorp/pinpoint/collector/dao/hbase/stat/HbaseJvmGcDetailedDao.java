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

package com.navercorp.pinpoint.collector.dao.hbase.stat;

import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import org.springframework.stereotype.Repository;

/**
 * @author HyunGil Jeong
 */
@Repository
public class HbaseJvmGcDetailedDao extends DefaultAgentStatDao<JvmGcDetailedBo> {

    public HbaseJvmGcDetailedDao(HbaseOperations2 hbaseTemplate,
                               TableNameProvider tableNameProvider,
                               AgentStatHbaseOperationFactory operationFactory,
                               AgentStatSerializer<JvmGcDetailedBo> serializer) {
        super(AgentStatType.JVM_GC_DETAILED, HbaseTable.AGENT_STAT_VER2, AgentStatBo::getJvmGcDetailedBos,
                hbaseTemplate, tableNameProvider, operationFactory, serializer);
    }

}
