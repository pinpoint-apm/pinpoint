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

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.inspector.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStat;
import com.navercorp.pinpoint.inspector.collector.model.kafka.AgentStatModelConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
@Configuration
public class PinotDaoConfiguration {

    private final KafkaTemplate kafkaAgentStatTemplate;
    private final String topic;

    public PinotDaoConfiguration(KafkaTemplate kafkaAgentStatTemplate, @Value("${kafka.inspector.topic}") String topic) {
        this.kafkaAgentStatTemplate = Objects.requireNonNull(kafkaAgentStatTemplate, "kafkaAgentStatTemplate");
        this.topic = topic;
    }

    private <T extends AgentStatDataPoint> AgentStatDao<T> newAgentStatDao(Function<AgentStatBo, List<T>> dataPointFunction, Function<List<T>, List<AgentStat>> convertToAgentStat) {
        return new DefaultAgentStatDao(dataPointFunction, kafkaAgentStatTemplate, convertToAgentStat, topic);
    }

    @Bean
    public AgentStatDao getPinotCpuLoadDao() {
        Function<List<CpuLoadBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertCpuLoadToAgentStat;
        return newAgentStatDao(AgentStatBo::getCpuLoadBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotActiveTraceDao() {
        Function<List<ActiveTraceBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertActiveTraceToAgentStat;
        return newAgentStatDao(AgentStatBo::getActiveTraceBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotJvmGcDao() {
        Function<List<JvmGcBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertJvmGcToAgentStat;
        return newAgentStatDao(AgentStatBo::getJvmGcBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotJvmGcDetailedDao() {
        Function<List<JvmGcDetailedBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertJvmGCDetailedToAgentStat;
        return newAgentStatDao(AgentStatBo::getJvmGcDetailedBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotTransactionDao() {
        Function<List<TransactionBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertTransactionToAgentStat;
        return newAgentStatDao(AgentStatBo::getTransactionBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotResponseTimeDao() {
        Function<List<ResponseTimeBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertResponseTimeToAgentStat;
        return newAgentStatDao(AgentStatBo::getResponseTimeBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotDeadlockThreadCountDao() {
        Function<List<DeadlockThreadCountBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertDeadlockThreadCountToAgentStat;
        return newAgentStatDao(AgentStatBo::getDeadlockThreadCountBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotFileDescriptorDao() {
        Function<List<FileDescriptorBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertFileDescriptorToAgentStat;
        return newAgentStatDao(AgentStatBo::getFileDescriptorBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotDirectBufferDao() {
        Function<List<DirectBufferBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertDirectBufferToAgentStat;
        return newAgentStatDao(AgentStatBo::getDirectBufferBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotTotalThreadCountDao() {
        Function<List<TotalThreadCountBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertTotalThreadCountToAgentStat;
        return newAgentStatDao(AgentStatBo::getTotalThreadCountBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotLoadedClassDao() {
        Function<List<LoadedClassBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertLoadedClassToAgentStat;
        return newAgentStatDao(AgentStatBo::getLoadedClassBos, convertToAgentStat);
    }

    @Bean
    public AgentStatDao getPinotDataSourceListDao() {
        Function<List<DataSourceListBo>, List<AgentStat>> convertToAgentStat = AgentStatModelConverter::convertDataSourceToAgentStat;
        return newAgentStatDao(AgentStatBo::getDataSourceListBos, convertToAgentStat);
    }
}
