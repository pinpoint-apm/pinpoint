package com.navercorp.pinpoint.common.server.bo.codec.stat;

import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AgentStatCoderConfiguration {

    @Bean
    public AgentStatDecoder<ActiveTraceBo> getAgentActiveTraceDecoder(List<AgentStatCodec<ActiveTraceBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<ActiveTraceBo> getAgentActiveTraceEncoder(AgentStatCodec<ActiveTraceBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<CpuLoadBo> getAgentCpuLoadDecoder(List<AgentStatCodec<CpuLoadBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<CpuLoadBo> getAgentCpuLoadEncoder(AgentStatCodec<CpuLoadBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<DataSourceListBo> getAgentDataSourceDecoder(List<AgentStatCodec<DataSourceListBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<DataSourceListBo> getAgentDataSourceEncoder(AgentStatCodec<DataSourceListBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<DeadlockThreadCountBo> getAgentDeadlockDecoder(List<AgentStatCodec<DeadlockThreadCountBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<DeadlockThreadCountBo> getAgentDeadlockEncoder(AgentStatCodec<DeadlockThreadCountBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<DirectBufferBo> getAgentDirectBufferDecoder(List<AgentStatCodec<DirectBufferBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<DirectBufferBo> getAgentDirectBufferEncoder(AgentStatCodec<DirectBufferBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<FileDescriptorBo> getAgentFileDescriptorDecoder(List<AgentStatCodec<FileDescriptorBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<FileDescriptorBo> getAgentFileDescriptorEncoder(AgentStatCodec<FileDescriptorBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<JvmGcBo> getAgentJvmGcDecoder(List<AgentStatCodec<JvmGcBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<JvmGcBo> getAgentJvmGcEncoder(AgentStatCodec<JvmGcBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<JvmGcDetailedBo> getAgentJvmGcDetailedDecoder(List<AgentStatCodec<JvmGcDetailedBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<JvmGcDetailedBo> getAgentJvmGcDetailedEncoder(AgentStatCodec<JvmGcDetailedBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<LoadedClassBo> getAgentLoadedClassCountDecoder(List<AgentStatCodec<LoadedClassBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<LoadedClassBo> getAgentLoadedClassCountEncoder(AgentStatCodec<LoadedClassBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<ResponseTimeBo> getAgentResponseTimeDecoder(List<AgentStatCodec<ResponseTimeBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<ResponseTimeBo> getAgentResponseTimeEncoder(AgentStatCodec<ResponseTimeBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<TotalThreadCountBo> getAgentTotalThreadCountDecoder(List<AgentStatCodec<TotalThreadCountBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<TotalThreadCountBo> getAgentTotalThreadCountEncoder(AgentStatCodec<TotalThreadCountBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<TransactionBo> getAgentTransactionDecoder(List<AgentStatCodec<TransactionBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<TransactionBo> getAgentTransactionEncoder(AgentStatCodec<TransactionBo> codec) {
        return new AgentStatEncoder<>(codec);
    }

    // ----------------

    @Bean
    public AgentStatDecoder<AgentUriStatBo> getAgentAgentUriStatDecoder(List<AgentStatCodec<AgentUriStatBo>> codecs) {
        return new AgentStatDecoder<>(codecs);
    }

    @Bean
    public AgentStatEncoder<AgentUriStatBo> getAgentAgentUriStatEncoder(AgentStatCodec<AgentUriStatBo> codec) {
        return new AgentStatEncoder<>(codec);
    }
}
