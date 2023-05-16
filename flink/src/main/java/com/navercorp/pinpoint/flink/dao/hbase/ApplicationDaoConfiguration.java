package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.ApplicationStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Configuration
public class ApplicationDaoConfiguration {

    private final HbaseTable tableName = HbaseTable.APPLICATION_STAT_AGGRE;
    private final HbaseTemplate2 hbaseTemplate2;
    private final ApplicationStatHbaseOperationFactory operations;
    private final TableNameProvider tableNameProvider;

    public ApplicationDaoConfiguration(HbaseTemplate2 hbaseTemplate2,
                                       ApplicationStatHbaseOperationFactory operations,
                                       TableNameProvider tableNameProvider) {
        this.hbaseTemplate2 = Objects.requireNonNull(hbaseTemplate2, "hbaseTemplate2");
        this.operations = Objects.requireNonNull(operations, "operations");
        this.tableNameProvider = Objects.requireNonNull(tableNameProvider, "tableNameProvider");
    }

    private <T extends JoinStatBo> ApplicationMetricDao<T> newApplicationMetricDao(StatType statType,
                                                                                   Function<JoinApplicationStatBo, List<T>> appStatFunction,
                                                                                   ApplicationStatSerializer<T> serializer) {
        Objects.requireNonNull(statType, "statType");
        Objects.requireNonNull(appStatFunction, "appStatFunction");
        Objects.requireNonNull(serializer, "serializer");

        return new DefaultApplicationMetricDao<>(statType, appStatFunction,
                serializer, tableName, hbaseTemplate2, operations, tableNameProvider);
    }

    @Bean
    public ApplicationMetricDao<JoinActiveTraceBo> getActiveTraceDao(ApplicationStatSerializer<JoinActiveTraceBo> serializer) {
        return newApplicationMetricDao(StatType.APP_ACTIVE_TRACE_COUNT, JoinApplicationStatBo::getJoinActiveTraceBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinCpuLoadBo> getCpuLoadDao(ApplicationStatSerializer<JoinCpuLoadBo> serializer) {
        return newApplicationMetricDao(StatType.APP_CPU_LOAD, JoinApplicationStatBo::getJoinCpuLoadBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinDataSourceListBo> getDataSourceDao(ApplicationStatSerializer<JoinDataSourceListBo> serializer) {
        return newApplicationMetricDao(StatType.APP_DATA_SOURCE, JoinApplicationStatBo::getJoinDataSourceListBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinDirectBufferBo> getDirectBufferDao(ApplicationStatSerializer<JoinDirectBufferBo> serializer) {
        return newApplicationMetricDao(StatType.APP_DIRECT_BUFFER, JoinApplicationStatBo::getJoinDirectBufferBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinFileDescriptorBo> getFileDescriptorDao(ApplicationStatSerializer<JoinFileDescriptorBo> serializer) {
        return newApplicationMetricDao(StatType.APP_FILE_DESCRIPTOR, JoinApplicationStatBo::getJoinFileDescriptorBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinLoadedClassBo> getLoadedClassDao(ApplicationStatSerializer<JoinLoadedClassBo> serializer) {
        return newApplicationMetricDao(StatType.APP_LOADED_CLASS, JoinApplicationStatBo::getJoinLoadedClassBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinMemoryBo> getMemoryDao(ApplicationStatSerializer<JoinMemoryBo> serializer) {
        return newApplicationMetricDao(StatType.APP_MEMORY_USED, JoinApplicationStatBo::getJoinMemoryBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinResponseTimeBo> getResponseTimeDao(ApplicationStatSerializer<JoinResponseTimeBo> serializer) {
        return newApplicationMetricDao(StatType.APP_RESPONSE_TIME, JoinApplicationStatBo::getJoinResponseTimeBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinTotalThreadCountBo> getTotalThreadCountDao(ApplicationStatSerializer<JoinTotalThreadCountBo> serializer) {
        return newApplicationMetricDao(StatType.APP_TOTAL_THREAD_COUNT, JoinApplicationStatBo::getJoinTotalThreadCountBoList, serializer);
    }

    @Bean
    public ApplicationMetricDao<JoinTransactionBo> getTransactionDao(ApplicationStatSerializer<JoinTransactionBo> serializer) {
        return newApplicationMetricDao(StatType.APP_TRANSACTION_COUNT, JoinApplicationStatBo::getJoinTransactionBoList, serializer);
    }
}
