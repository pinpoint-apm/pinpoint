/*
 * Copyright 2020 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import org.springframework.stereotype.Repository;

@Repository
public class HBaseLoadedClassDao extends DefaultAgentStatDao<LoadedClassBo> {

    public HBaseLoadedClassDao(HbaseOperations2 hbaseTemplate,
                               TableNameProvider tableNameProvider,
                               AgentStatHbaseOperationFactory operationFactory,
                               AgentStatSerializer<LoadedClassBo> serializer) {
        super(AgentStatType.LOADED_CLASS, HbaseTable.AGENT_STAT_VER2, AgentStatBo::getLoadedClassBos,
                hbaseTemplate, tableNameProvider, operationFactory, serializer);
    }


}
