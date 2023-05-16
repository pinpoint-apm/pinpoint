package com.navercorp.pinpoint.web.dao.hbase.config;

import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
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
import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;
import com.navercorp.pinpoint.web.dao.hbase.appmetric.DefaultApplicationMetricDao;
import com.navercorp.pinpoint.web.dao.hbase.appmetric.HbaseApplicationStatDaoOperations;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.ApplicationStatSampler;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinCpuLoadBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceListBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.AggregationStatData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class ApplicationMetricConfiguration {
    private final HbaseApplicationStatDaoOperations operations;

    public ApplicationMetricConfiguration(HbaseApplicationStatDaoOperations operations) {
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    private <IN extends JoinStatBo, OUT extends AggregationStatData> ApplicationMetricDao<OUT> newApplicationMetricDao(StatType statType,
                                                                                                                       ApplicationStatDecoder<IN> decoder,
                                                                                                                       ApplicationStatSampler<IN, OUT> sampler) {
        Objects.requireNonNull(statType, "statType");
        Objects.requireNonNull(decoder, "decoder");
        Objects.requireNonNull(sampler, "sampler");
        return new DefaultApplicationMetricDao<>(statType, decoder, sampler, operations);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinActiveTraceBo> getApplicationActiveTraceDao(ApplicationStatDecoder<JoinActiveTraceBo> decoder,
                                                                                     ApplicationStatSampler<JoinActiveTraceBo, AggreJoinActiveTraceBo> sampler) {
        return newApplicationMetricDao(StatType.APP_ACTIVE_TRACE_COUNT, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinCpuLoadBo> getApplicationCpuLoadDao(ApplicationStatDecoder<JoinCpuLoadBo> decoder,
                                                                             ApplicationStatSampler<JoinCpuLoadBo, AggreJoinCpuLoadBo> sampler) {
        return newApplicationMetricDao(StatType.APP_CPU_LOAD, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinDataSourceListBo> getApplicationDataSourceDao(ApplicationStatDecoder<JoinDataSourceListBo> decoder,
                                                                                       ApplicationStatSampler<JoinDataSourceListBo, AggreJoinDataSourceListBo> sampler) {
        return newApplicationMetricDao(StatType.APP_DATA_SOURCE, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinDirectBufferBo> getApplicationDirectBufferDao(ApplicationStatDecoder<JoinDirectBufferBo> decoder,
                                                                                       ApplicationStatSampler<JoinDirectBufferBo, AggreJoinDirectBufferBo> sampler) {
        return newApplicationMetricDao(StatType.APP_DIRECT_BUFFER, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinFileDescriptorBo> getApplicationFileDescriptorDao(ApplicationStatDecoder<JoinFileDescriptorBo> decoder,
                                                                                           ApplicationStatSampler<JoinFileDescriptorBo, AggreJoinFileDescriptorBo> sampler) {
        return newApplicationMetricDao(StatType.APP_FILE_DESCRIPTOR, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinLoadedClassBo> getApplicationLoadedClassDao(ApplicationStatDecoder<JoinLoadedClassBo> decoder,
                                                                                     ApplicationStatSampler<JoinLoadedClassBo, AggreJoinLoadedClassBo> sampler) {
        return newApplicationMetricDao(StatType.APP_LOADED_CLASS, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinMemoryBo> getApplicationMemoryDao(ApplicationStatDecoder<JoinMemoryBo> decoder,
                                                                           ApplicationStatSampler<JoinMemoryBo, AggreJoinMemoryBo> sampler) {
        return newApplicationMetricDao(StatType.APP_MEMORY_USED, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinResponseTimeBo> getApplicationResponseTimeDao(ApplicationStatDecoder<JoinResponseTimeBo> decoder,
                                                                                       ApplicationStatSampler<JoinResponseTimeBo, AggreJoinResponseTimeBo> sampler) {
        return newApplicationMetricDao(StatType.APP_RESPONSE_TIME, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinTotalThreadCountBo> getApplicationTotalThreadCountDao(ApplicationStatDecoder<JoinTotalThreadCountBo> decoder,
                                                                                               ApplicationStatSampler<JoinTotalThreadCountBo, AggreJoinTotalThreadCountBo> sampler) {
        return newApplicationMetricDao(StatType.APP_TOTAL_THREAD_COUNT, decoder, sampler);
    }

    @Bean
    public ApplicationMetricDao<AggreJoinTransactionBo> getApplicationTransactionDao(ApplicationStatDecoder<JoinTransactionBo> decoder,
                                                                                     ApplicationStatSampler<JoinTransactionBo, AggreJoinTransactionBo> sampler) {
        return newApplicationMetricDao(StatType.APP_TRANSACTION_COUNT, decoder, sampler);
    }
}
