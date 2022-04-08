package com.navercorp.pinpoint.web.dao.hbase.config;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
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
import com.navercorp.pinpoint.web.dao.hbase.stat.DefaultAgentStatDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.HbaseAgentStatDaoOperations;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class AgentStatDaoConfiguration {
    private final HbaseAgentStatDaoOperations operations;

    public AgentStatDaoConfiguration(HbaseAgentStatDaoOperations operations) {
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    private <T extends AgentStatDataPoint> AgentStatDao<T> newStatDao(AgentStatType statType, AgentStatDecoder<T> decoder) {
        Objects.requireNonNull(statType, "statType");
        Objects.requireNonNull(decoder, "decoder");

        return new DefaultAgentStatDao<>(statType, operations, decoder);
    }

    @Bean
    public AgentStatDao<JvmGcBo> getJvmGcDao(AgentStatDecoder<JvmGcBo> decoder) {
        return newStatDao(AgentStatType.JVM_GC, decoder);
    }

    @Bean
    public AgentStatDao<JvmGcDetailedBo> getJvmGcDetailedDao(AgentStatDecoder<JvmGcDetailedBo> decoder) {
        return newStatDao(AgentStatType.JVM_GC_DETAILED, decoder);
    }

    @Bean
    public AgentStatDao<CpuLoadBo> getCpuLoadDao(AgentStatDecoder<CpuLoadBo> decoder) {
        return newStatDao(AgentStatType.CPU_LOAD, decoder);
    }

    @Bean
    public AgentStatDao<TransactionBo> getTransactionDao(AgentStatDecoder<TransactionBo> decoder) {
        return newStatDao(AgentStatType.TRANSACTION, decoder);
    }

    @Bean
    public AgentStatDao<ActiveTraceBo> getActiveTraceDao(AgentStatDecoder<ActiveTraceBo> decoder) {
        return newStatDao(AgentStatType.ACTIVE_TRACE, decoder);
    }

    @Bean
    public AgentStatDao<DataSourceListBo> getDataSourceListDao(AgentStatDecoder<DataSourceListBo> decoder) {
        return newStatDao(AgentStatType.DATASOURCE, decoder);
    }

    @Bean
    public AgentStatDao<ResponseTimeBo> getResponseTimeDao(AgentStatDecoder<ResponseTimeBo> decoder) {
        return newStatDao(AgentStatType.RESPONSE_TIME, decoder);
    }

    @Bean
    public AgentStatDao<DeadlockThreadCountBo> getDeadlockThreadCountDao(AgentStatDecoder<DeadlockThreadCountBo> decoder) {
        return newStatDao(AgentStatType.DEADLOCK, decoder);
    }

    @Bean
    public AgentStatDao<FileDescriptorBo> getFileDescriptorDao(AgentStatDecoder<FileDescriptorBo> decoder) {
        return newStatDao(AgentStatType.FILE_DESCRIPTOR, decoder);
    }

    @Bean
    public AgentStatDao<DirectBufferBo> getDirectBufferDao(AgentStatDecoder<DirectBufferBo> decoder) {
        return newStatDao(AgentStatType.DIRECT_BUFFER, decoder);
    }

    @Bean
    public AgentStatDao<TotalThreadCountBo> getTotalThreadCountDao(AgentStatDecoder<TotalThreadCountBo> decoder) {
        return newStatDao(AgentStatType.TOTAL_THREAD, decoder);
    }

    @Bean
    public AgentStatDao<LoadedClassBo> getLoadedClassDao(AgentStatDecoder<LoadedClassBo> decoder) {
        return newStatDao(AgentStatType.LOADED_CLASS, decoder);
    }

    @Bean
    public AgentStatDao<AgentUriStatBo> getAgentUriStatDao(AgentStatDecoder<AgentUriStatBo> decoder) {
        return newStatDao(AgentStatType.URI, decoder);
    }
}
