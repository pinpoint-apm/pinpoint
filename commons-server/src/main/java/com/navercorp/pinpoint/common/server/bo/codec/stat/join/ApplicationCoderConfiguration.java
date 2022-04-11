package com.navercorp.pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatDecoder;
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

import java.util.List;

@Configuration
public class ApplicationCoderConfiguration {

    @Bean
    public ApplicationStatDecoder<JoinActiveTraceBo> getActiveTraceDecoder(List<ApplicationStatCodec<JoinActiveTraceBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinActiveTraceBo> getActiveTraceEncoder(ApplicationStatCodec<JoinActiveTraceBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinCpuLoadBo> getCpuLoadDecoder(List<ApplicationStatCodec<JoinCpuLoadBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinCpuLoadBo> getCpuLoadEncoder(ApplicationStatCodec<JoinCpuLoadBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinDataSourceListBo> getDataSourceDecoder(List<ApplicationStatCodec<JoinDataSourceListBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinDataSourceListBo> getDataSourceEncoder(ApplicationStatCodec<JoinDataSourceListBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinDirectBufferBo> getDirectBufferDecoder(List<ApplicationStatCodec<JoinDirectBufferBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinDirectBufferBo> getDirectBufferEncoder(ApplicationStatCodec<JoinDirectBufferBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }


    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinFileDescriptorBo> getFileDescriptorDecoder(List<ApplicationStatCodec<JoinFileDescriptorBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinFileDescriptorBo> getFileDescriptorEncoder(ApplicationStatCodec<JoinFileDescriptorBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinLoadedClassBo> getLoadedClassDecoder(List<ApplicationStatCodec<JoinLoadedClassBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinLoadedClassBo> getLoadedClassEncoder(ApplicationStatCodec<JoinLoadedClassBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinMemoryBo> getMemoryDecoder(List<ApplicationStatCodec<JoinMemoryBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinMemoryBo> getMemoryEncoder(ApplicationStatCodec<JoinMemoryBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinResponseTimeBo> getResponseTimeDecoder(List<ApplicationStatCodec<JoinResponseTimeBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinResponseTimeBo> getResponseTimeEncoder(ApplicationStatCodec<JoinResponseTimeBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinTotalThreadCountBo> getTotalThreadCountDecoder(List<ApplicationStatCodec<JoinTotalThreadCountBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinTotalThreadCountBo> getTotalThreadCountEncoder(ApplicationStatCodec<JoinTotalThreadCountBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }

    //-----------------

    @Bean
    public ApplicationStatDecoder<JoinTransactionBo> getTransactionDecoder(List<ApplicationStatCodec<JoinTransactionBo>> codecs) {
        return new ApplicationStatDecoder<>(codecs);
    }

    @Bean
    public ApplicationStatEncoder<JoinTransactionBo> getTransactionEncoder(ApplicationStatCodec<JoinTransactionBo> codec) {
        return new ApplicationStatEncoder<>(codec);
    }
}
