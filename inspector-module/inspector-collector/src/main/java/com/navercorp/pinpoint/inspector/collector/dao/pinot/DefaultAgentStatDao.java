/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.collector.dao.pinot;

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
public class DefaultAgentStatDao <T extends AgentStatDataPoint> implements AgentStatDao<T> {

    private final Logger logger = LogManager.getLogger(DefaultAgentStatDao.class.getName());

    private final Function<AgentStatBo, List<T>> dataPointFunction;
    private final BiFunction<List<T>, String, List<AgentStat>> convertToKafkaModelFunction;
    private final KafkaTemplate kafkaAgentStatTemplate;
    private final String topic;
    private final TenantProvider tenantProvider;

    public DefaultAgentStatDao(Function<AgentStatBo, List<T>> dataPointFunction, KafkaTemplate kafkaAgentStatTemplate, BiFunction<List<T>, String, List<AgentStat>> convertToKafkaModelFunction, String topic, TenantProvider tenantProvider) {
        this.dataPointFunction = Objects.requireNonNull(dataPointFunction, "dataPointFunction");
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.convertToKafkaModelFunction = convertToKafkaModelFunction;
        this.topic = topic;
        this.tenantProvider = tenantProvider;
    }

    @Override
    public void insert(String agentId, List<T> agentStatData) {
        List<AgentStat> agentStatList = convertDataToKafkaModel(agentStatData);
        for (AgentStat agentStat : agentStatList) {
            kafkaAgentStatTemplate.send(topic, agentStat.getSortKey(), agentStat);
        }
    }

    private List<AgentStat> convertDataToKafkaModel(List<T> AgentStatDataPointList) {
        List<AgentStat> agentStatList = convertToKafkaModelFunction.apply(AgentStatDataPointList, tenantProvider.getTenantId());
        return agentStatList;
    }

    @Override
    public void dispatch(AgentStatBo agentStatBo) {
        Objects.requireNonNull(agentStatBo, "agentStatBo");
        List<T> dataPointList = this.dataPointFunction.apply(agentStatBo);
        insert(agentStatBo.getAgentId(), dataPointList);
    }
}
