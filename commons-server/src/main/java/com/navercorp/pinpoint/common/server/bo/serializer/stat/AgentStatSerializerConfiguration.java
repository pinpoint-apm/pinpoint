package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatEncoder;
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceBo;
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

@Configuration
public class AgentStatSerializerConfiguration {
    @Bean
    public AgentStatSerializer<ActiveTraceBo> getAgentActiveTraceSerializer(AgentStatEncoder<ActiveTraceBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<CpuLoadBo> getAgentCpuLoadSerializer(AgentStatEncoder<CpuLoadBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<DataSourceListBo> getAgentDataSourceSerializer(AgentStatEncoder<DataSourceListBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<DeadlockThreadCountBo> getAgentDeadlockThreadCountSerializer(AgentStatEncoder<DeadlockThreadCountBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<DirectBufferBo> getAgentDirectBufferSerializer(AgentStatEncoder<DirectBufferBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<FileDescriptorBo> getAgentFileDescriptorSerializer(AgentStatEncoder<FileDescriptorBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<JvmGcDetailedBo> getAgentJvmGcDetailedSerializer(AgentStatEncoder<JvmGcDetailedBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<JvmGcBo> getAgentJvmGcSerializer(AgentStatEncoder<JvmGcBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<LoadedClassBo> getAgentLoadedClassSerializer(AgentStatEncoder<LoadedClassBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<ResponseTimeBo> getAgentResponseTimeSerializer(AgentStatEncoder<ResponseTimeBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<TotalThreadCountBo> getAgentTotalThreadCountSerializer(AgentStatEncoder<TotalThreadCountBo> coder) {
        return new AgentStatSerializer<>(coder);
    }

    @Bean
    public AgentStatSerializer<TransactionBo> getAgentTransactionSerializer(AgentStatEncoder<TransactionBo> coder) {
        return new AgentStatSerializer<>(coder);
    }
}
