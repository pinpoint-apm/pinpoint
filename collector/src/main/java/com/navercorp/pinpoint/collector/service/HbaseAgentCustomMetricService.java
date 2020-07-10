/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.util.CollectorUtils;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.codec.metric.CustomMetricCodec;
import com.navercorp.pinpoint.common.server.bo.codec.metric.CustomMetricEncoder;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricType;
import com.navercorp.pinpoint.common.server.bo.metric.EachCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.serializer.metric.CustomMetricSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class HbaseAgentCustomMetricService extends AbstractAgentCustomMetricService {

    private final HbaseOperations2 hbaseTemplate;

    private final TableNameProvider tableNameProvider;

    private final AgentStatHbaseOperationFactory agentStatHbaseOperationFactory;

    private final CustomMetricSerializer serializer;

    public HbaseAgentCustomMetricService(HbaseOperations2 hbaseTemplate, TableNameProvider tableNameProvider, AgentStatHbaseOperationFactory agentStatHbaseOperationFactory,
                                         CustomMetricType customMetricType) {
        super(customMetricType);

        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");

        this.agentStatHbaseOperationFactory = Objects.requireNonNull(agentStatHbaseOperationFactory, "agentStatHbaseOperationFactory");

        CustomMetricCodec customMetricCodec = new CustomMetricCodec(new AgentStatDataPointCodec(), customMetricType);
        CustomMetricEncoder customMetricEncoder = new CustomMetricEncoder(customMetricCodec);
        CustomMetricSerializer customMetricSerializer = new CustomMetricSerializer(customMetricEncoder);

        this.serializer = customMetricSerializer;
    }

    @Override
    public void save(String agentId, List<EachCustomMetricBo> eachCustomMetricBoList) {
        Objects.requireNonNull(agentId, "agentId");
        // Assert agentId
        CollectorUtils.checkAgentId(agentId);

        if (CollectionUtils.isEmpty(eachCustomMetricBoList)) {
            return;
        }

        List<Put> puts = this.agentStatHbaseOperationFactory.createPuts(agentId, getCustomMetricType().getAgentStatType(), eachCustomMetricBoList, serializer);
        if (!puts.isEmpty()) {
            TableName agentStatTableName = tableNameProvider.getTableName(HbaseTable.AGENT_STAT_VER2);
            this.hbaseTemplate.asyncPut(agentStatTableName, puts);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HbaseAgentCustomMetricService{");
        sb.append("customMetricType=").append(customMetricType);
        sb.append('}');
        return sb.toString();
    }
}
