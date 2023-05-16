package com.navercorp.pinpoint.collector.dao.hbase.stat;

import com.navercorp.pinpoint.collector.dao.AgentStatDao;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Configuration
public class HBaseDaoConfiguration {

    private final HbaseOperations2 hbaseTemplate;
    private final HbaseTable hbaseTable = HbaseTable.AGENT_STAT_VER2;
    private final TableNameProvider tableNameProvider;
    private final AgentStatHbaseOperationFactory operations;

    public HBaseDaoConfiguration(HbaseOperations2 hbaseTemplate, TableNameProvider tableNameProvider, AgentStatHbaseOperationFactory operations) {
        this.hbaseTemplate = Objects.requireNonNull(hbaseTemplate, "hbaseTemplate");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    private <T extends AgentStatDataPoint> AgentStatDao<T> newAgentStatDao(AgentStatType agentStatType, Function<AgentStatBo, List<T>> dataPointFunction, AgentStatSerializer<T> serializer) {
        return new DefaultAgentStatDao<>(agentStatType, hbaseTable, dataPointFunction,
                hbaseTemplate, tableNameProvider, operations, serializer);
    }

    @Bean
    public AgentStatDao<ActiveTraceBo> getActiveTraceDao(AgentStatSerializer<ActiveTraceBo> serializer) {
        return newAgentStatDao(AgentStatType.ACTIVE_TRACE, AgentStatBo::getActiveTraceBos, serializer);
    }

    @Bean
    public AgentStatDao<CpuLoadBo> getCpuLoadDao(AgentStatSerializer<CpuLoadBo> serializer) {
        return newAgentStatDao(AgentStatType.CPU_LOAD, AgentStatBo::getCpuLoadBos, serializer);
    }

    @Bean
    public AgentStatDao<DataSourceListBo> getDataSourceListDao(AgentStatSerializer<DataSourceListBo> serializer) {
        return new HbaseDataSourceListDao(hbaseTemplate, tableNameProvider, operations, serializer);
    }

    @Bean
    public AgentStatDao<DeadlockThreadCountBo> getDeadlockThreadCountDao(AgentStatSerializer<DeadlockThreadCountBo> serializer) {
        return newAgentStatDao(AgentStatType.DEADLOCK, AgentStatBo::getDeadlockThreadCountBos, serializer);
    }

    @Bean
    public AgentStatDao<DirectBufferBo> getDirectBufferDao(AgentStatSerializer<DirectBufferBo> serializer) {
        return newAgentStatDao(AgentStatType.DIRECT_BUFFER, AgentStatBo::getDirectBufferBos, serializer);
    }

    @Bean
    public AgentStatDao<FileDescriptorBo> getFileDescriptorDao(AgentStatSerializer<FileDescriptorBo> serializer) {
        return newAgentStatDao(AgentStatType.FILE_DESCRIPTOR, AgentStatBo::getFileDescriptorBos, serializer);
    }

    @Bean
    public AgentStatDao<JvmGcBo> getJvmGcDao(AgentStatSerializer<JvmGcBo> serializer) {
        return newAgentStatDao(AgentStatType.JVM_GC, AgentStatBo::getJvmGcBos, serializer);
    }

    @Bean
    public AgentStatDao<JvmGcDetailedBo> getJvmGcDetailedDao(AgentStatSerializer<JvmGcDetailedBo> serializer) {
        return newAgentStatDao(AgentStatType.JVM_GC_DETAILED, AgentStatBo::getJvmGcDetailedBos, serializer);
    }

    @Bean
    public AgentStatDao<LoadedClassBo> getLoadedClassDao(AgentStatSerializer<LoadedClassBo> serializer) {
        return newAgentStatDao(AgentStatType.LOADED_CLASS, AgentStatBo::getLoadedClassBos, serializer);
    }

    @Bean
    public AgentStatDao<ResponseTimeBo> getResponseTimeDao(AgentStatSerializer<ResponseTimeBo> serializer) {
        return newAgentStatDao(AgentStatType.RESPONSE_TIME, AgentStatBo::getResponseTimeBos, serializer);
    }

    @Bean
    public AgentStatDao<TotalThreadCountBo> getTotalThreadCountDao(AgentStatSerializer<TotalThreadCountBo> serializer) {
        return newAgentStatDao(AgentStatType.TOTAL_THREAD, AgentStatBo::getTotalThreadCountBos, serializer);
    }

    @Bean
    public AgentStatDao<TransactionBo> getTransactionDao(AgentStatSerializer<TransactionBo> serializer) {
        return newAgentStatDao(AgentStatType.TRANSACTION, AgentStatBo::getTransactionBos, serializer);
    }
}
