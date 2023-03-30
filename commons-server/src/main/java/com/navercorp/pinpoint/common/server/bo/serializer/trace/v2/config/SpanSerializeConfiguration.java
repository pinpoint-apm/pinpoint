package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.config;

import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanChunkSerializerV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoderV0;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanSerializerV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.TraceRowKeyDecoderV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.TraceRowKeyEncoderV2;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpanSerializeConfiguration {
    @Bean
    public SpanChunkSerializerV2 spanChunkSerializerV2(SpanEncoder spanEncoder) {
        return new SpanChunkSerializerV2(spanEncoder);
    }

    @Bean
    public SpanSerializerV2 spanSerializerV2(SpanEncoder spanEncoder) {
        return new SpanSerializerV2(spanEncoder);
    }

    @Bean
    public SpanDecoderV0 spanDecoderV0() {
        return new SpanDecoderV0();
    }

    @Bean
    public SpanEncoderV0 spanEncoderV0() {
        return new SpanEncoderV0();
    }


    @Bean
    public TraceRowKeyDecoderV2 traceRowKeyDecoderV2() {
        return new TraceRowKeyDecoderV2();
    }

    @Bean
    public TraceRowKeyEncoderV2 traceRowKeyEncoderV2(@Qualifier("traceV2Distributor") AbstractRowKeyDistributor rowKeyDistributor) {
        return new TraceRowKeyEncoderV2(rowKeyDistributor);
    }


}
