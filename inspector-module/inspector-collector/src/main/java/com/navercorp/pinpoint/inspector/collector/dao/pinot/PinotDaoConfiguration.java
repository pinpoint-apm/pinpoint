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
import com.navercorp.pinpoint.inspector.collector.config.InspectorCollectorProperties;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStatModelConverter;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.ApplicationStatModelConverter;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
@Configuration
public class PinotDaoConfiguration {

    private final KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate;
    private final KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate;
    InspectorCollectorProperties inspectorCollectorProperties;
    private final TenantProvider tenantProvider;

    public PinotDaoConfiguration(KafkaTemplate<String, AgentStat> kafkaAgentStatTemplate, KafkaTemplate<String, ApplicationStat> kafkaApplicationStatTemplate, InspectorCollectorProperties inspectorCollectorProperties, TenantProvider tenantProvider) {
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.kafkaApplicationStatTemplate = Objects.requireNonNull(kafkaApplicationStatTemplate, "kafkaApplicationStatTemplate");
        this.inspectorCollectorProperties = Objects.requireNonNull(inspectorCollectorProperties, "inspectorCollectorProperties");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    private <T extends AgentStatDataPoint> AgentStatDao<T> newAgentStatDao(Function<AgentStatBo, List<T>> dataPointFunction, BiFunction<List<T>, String, List<AgentStat>> convertToAgentStat, Function<List<AgentStat>, List<ApplicationStat>> convertToKafkaApplicationStat) {
        return new DefaultAgentStatDao(dataPointFunction, kafkaAgentStatTemplate, kafkaApplicationStatTemplate, convertToAgentStat, convertToKafkaApplicationStat, inspectorCollectorProperties, tenantProvider);
    }

    @Bean
    public AgentStatDao getPinotCpuLoadDao() {
        return newAgentStatDao(AgentStatBo::getCpuLoadBos, AgentStatModelConverter::convertCpuLoadToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotActiveTraceDao() {
        return newAgentStatDao(AgentStatBo::getActiveTraceBos, AgentStatModelConverter::convertActiveTraceToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotJvmGcDao() {
        return newAgentStatDao(AgentStatBo::getJvmGcBos, AgentStatModelConverter::convertJvmGcToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotJvmGcDetailedDao() {
        return newAgentStatDao(AgentStatBo::getJvmGcDetailedBos, AgentStatModelConverter::convertJvmGCDetailedToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotTransactionDao() {
        return newAgentStatDao(AgentStatBo::getTransactionBos, AgentStatModelConverter::convertTransactionToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotResponseTimeDao() {
        return newAgentStatDao(AgentStatBo::getResponseTimeBos, AgentStatModelConverter::convertResponseTimeToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotDeadlockThreadCountDao() {
        return newAgentStatDao(AgentStatBo::getDeadlockThreadCountBos, AgentStatModelConverter::convertDeadlockThreadCountToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotFileDescriptorDao() {
        return newAgentStatDao(AgentStatBo::getFileDescriptorBos, AgentStatModelConverter::convertFileDescriptorToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotDirectBufferDao() {
        return newAgentStatDao(AgentStatBo::getDirectBufferBos, AgentStatModelConverter::convertDirectBufferToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotTotalThreadCountDao() {
        return newAgentStatDao(AgentStatBo::getTotalThreadCountBos, AgentStatModelConverter::convertTotalThreadCountToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotLoadedClassDao() {
        return newAgentStatDao(AgentStatBo::getLoadedClassBos, AgentStatModelConverter::convertLoadedClassToAgentStat, ApplicationStatModelConverter::convertToApplicationStat);
    }

    @Bean
    public AgentStatDao getPinotDataSourceListDao() {
        return newAgentStatDao(AgentStatBo::getDataSourceListBos, AgentStatModelConverter::convertDataSourceToAgentStat, ApplicationStatModelConverter::convertFromDataSourceStatToApplicationStat);
    }
}
