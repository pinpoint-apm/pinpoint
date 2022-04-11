package com.navercorp.pinpoint.common.server.bo.serializer.stat.join;

import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatEncoder;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinCpuLoadBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationStatSerializerConfiguration {

    @Bean
    public ApplicationStatSerializer<JoinActiveTraceBo> getAppActiveTraceSerializer(ApplicationStatEncoder<JoinActiveTraceBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinCpuLoadBo> getAppCpuLoadSerializer(ApplicationStatEncoder<JoinCpuLoadBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinDataSourceListBo> getAppDataSourceSerializer(ApplicationStatEncoder<JoinDataSourceListBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinDirectBufferBo> getAppDirectBufferSerializer(ApplicationStatEncoder<JoinDirectBufferBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinFileDescriptorBo> getAppFileDescriptorSerializer(ApplicationStatEncoder<JoinFileDescriptorBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinLoadedClassBo> getAppLoadedClassSerializer(ApplicationStatEncoder<JoinLoadedClassBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinMemoryBo> getAppMemorySerializer(ApplicationStatEncoder<JoinMemoryBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinResponseTimeBo> getAppResponseTimeSerializer(ApplicationStatEncoder<JoinResponseTimeBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinTotalThreadCountBo> getAppTotalThreadCountSerializer(ApplicationStatEncoder<JoinTotalThreadCountBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }

    @Bean
    public ApplicationStatSerializer<JoinTransactionBo> getAppTransactionSerializer(ApplicationStatEncoder<JoinTransactionBo> coder) {
        return new ApplicationStatSerializer<>(coder);
    }
}

