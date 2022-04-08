package com.navercorp.pinpoint.web.dao.hbase.config;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.CpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.FileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.common.server.bo.stat.LoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.ResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.TotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.DefaultSampledAgentStatDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.HbaseAgentStatDaoOperations;
import com.navercorp.pinpoint.web.dao.hbase.stat.HbaseAgentUriStatDaoOperations;
import com.navercorp.pinpoint.web.dao.hbase.stat.HbaseSampledAgentUriStatDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.HbaseSampledDataSourceDao;
import com.navercorp.pinpoint.web.dao.hbase.stat.SampledAgentStatResultExtractorSupplier;
import com.navercorp.pinpoint.web.dao.hbase.stat.SampledResultsExtractorSupplier;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.AgentStatSampler;
import com.navercorp.pinpoint.web.vo.stat.SampledActiveTrace;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentUriStat;
import com.navercorp.pinpoint.web.vo.stat.SampledCpuLoad;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import com.navercorp.pinpoint.web.vo.stat.SampledEachUriStatBo;
import com.navercorp.pinpoint.web.vo.stat.SampledFileDescriptor;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGc;
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.SampledLoadedClassCount;
import com.navercorp.pinpoint.web.vo.stat.SampledResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledTotalThreadCount;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class SampledAgentStatDaoConfiguration {

    private final HbaseAgentStatDaoOperations operations;

    public SampledAgentStatDaoConfiguration(HbaseAgentStatDaoOperations operations) {
        this.operations = Objects.requireNonNull(operations, "operations");
    }


    private <IN extends AgentStatDataPoint,
            OUT extends SampledAgentStatDataPoint> SampledAgentStatDao<OUT> newSampledDao(AgentStatType statType,
                                                                                          AgentStatDecoder<IN> decoder,
                                                                                          AgentStatSampler<IN, OUT> sampler) {
        Objects.requireNonNull(statType, "statType");
        Objects.requireNonNull(decoder, "decoder");
        Objects.requireNonNull(sampler, "sampler");

        SampledResultsExtractorSupplier<IN, OUT> supplier = new SampledAgentStatResultExtractorSupplier<>(sampler);
        return new DefaultSampledAgentStatDao<>(statType, operations, decoder, supplier);
    }

    @Bean
    public SampledAgentStatDao<SampledJvmGc> getSampledJvmGcDao(AgentStatDecoder<JvmGcBo> decoder,
                                                                AgentStatSampler<JvmGcBo, SampledJvmGc> sampler) {
        return newSampledDao(AgentStatType.JVM_GC, decoder, sampler);
    }

    @Bean
    public SampledAgentStatDao<SampledJvmGcDetailed> getSampledJvmGcDetailedDao(AgentStatDecoder<JvmGcDetailedBo> decoder,
                                                                                AgentStatSampler<JvmGcDetailedBo, SampledJvmGcDetailed> sampler) {
        return newSampledDao(AgentStatType.JVM_GC_DETAILED, decoder, sampler);
    }

    @Bean
    public SampledAgentStatDao<SampledCpuLoad> getSampledCpuLoadDao(AgentStatDecoder<CpuLoadBo> decoder,
                                                                    AgentStatSampler<CpuLoadBo, SampledCpuLoad> sampler) {
        return newSampledDao(AgentStatType.CPU_LOAD, decoder, sampler);
    }

    @Bean
    public SampledAgentStatDao<SampledTransaction> getSampledTransactionDao(AgentStatDecoder<TransactionBo> decoder,
                                                                            AgentStatSampler<TransactionBo, SampledTransaction> sampler) {
        return newSampledDao(AgentStatType.TRANSACTION, decoder, sampler);
    }


    @Bean
    public SampledAgentStatDao<SampledActiveTrace> getSampledActiveTraceDao(AgentStatDecoder<ActiveTraceBo> decoder,
                                                                            AgentStatSampler<ActiveTraceBo, SampledActiveTrace> sampler) {
        return newSampledDao(AgentStatType.ACTIVE_TRACE, decoder, sampler);
    }

//    @Bean
    public SampledAgentStatDao<SampledDataSourceList> getSampledDataSourceListDao(AgentStatDecoder<DataSourceListBo> decoder,
                                                                                  AgentStatSampler<DataSourceBo, SampledDataSource> sampler) {
        return new HbaseSampledDataSourceDao(operations, decoder, sampler);
    }

    @Bean
    public SampledAgentStatDao<SampledResponseTime> getSampledResponseTimeDao(AgentStatDecoder<ResponseTimeBo> decoder,
                                                                              AgentStatSampler<ResponseTimeBo, SampledResponseTime> sampler) {
        return newSampledDao(AgentStatType.RESPONSE_TIME, decoder, sampler);
    }

    @Bean
    public SampledAgentStatDao<SampledDeadlock> getSampledDeadlockDao(AgentStatDecoder<DeadlockThreadCountBo> decoder,
                                                                      AgentStatSampler<DeadlockThreadCountBo, SampledDeadlock> sampler) {
        return newSampledDao(AgentStatType.DEADLOCK, decoder, sampler);
    }

    @Bean
    public SampledAgentStatDao<SampledFileDescriptor> getSampledFileDescriptorDao(AgentStatDecoder<FileDescriptorBo> decoder,
                                                                                  AgentStatSampler<FileDescriptorBo, SampledFileDescriptor> sampler) {
        return newSampledDao(AgentStatType.FILE_DESCRIPTOR, decoder, sampler);
    }


    @Bean
    public SampledAgentStatDao<SampledDirectBuffer> getSampledDirectBufferDao(AgentStatDecoder<DirectBufferBo> decoder,
                                                                              AgentStatSampler<DirectBufferBo, SampledDirectBuffer> sampler) {
        return newSampledDao(AgentStatType.DIRECT_BUFFER, decoder, sampler);
    }

    @Bean
    public SampledAgentStatDao<SampledTotalThreadCount> getSampledTotalThreadCountDao(AgentStatDecoder<TotalThreadCountBo> decoder,
                                                                                      AgentStatSampler<TotalThreadCountBo, SampledTotalThreadCount> sampler) {
        return newSampledDao(AgentStatType.TOTAL_THREAD, decoder, sampler);
    }


    @Bean
    public SampledAgentStatDao<SampledLoadedClassCount> getSampledLoadedClassCountDao(AgentStatDecoder<LoadedClassBo> decoder,
                                                                                      AgentStatSampler<LoadedClassBo, SampledLoadedClassCount> sampler) {
        return newSampledDao(AgentStatType.LOADED_CLASS, decoder, sampler);
    }

//    @Bean
    public SampledAgentStatDao<SampledAgentUriStat> getSampledAgentUriStatDao(HbaseAgentUriStatDaoOperations operations,
                                                                              AgentStatDecoder<AgentUriStatBo> decoder,
                                                                              AgentStatSampler<EachUriStatBo, SampledEachUriStatBo> sampler) {
        return new HbaseSampledAgentUriStatDao(operations, decoder, sampler);
    }
}