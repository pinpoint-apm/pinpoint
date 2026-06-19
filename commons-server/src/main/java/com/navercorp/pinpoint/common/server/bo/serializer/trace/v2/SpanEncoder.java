package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

import java.nio.ByteBuffer;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface SpanEncoder {

    byte TYPE_SPAN = 0;
    byte TYPE_SPAN_CHUNK = 1;

    // OpenTelemetry-origin spans. The first byte of the qualifier doubles as both the
    // span/chunk discriminator and the source-type discriminator (Pinpoint vs OTel).
    byte TYPE_OTEL_SPAN = 2;
    byte TYPE_OTEL_SPAN_CHUNK = 3;

    // reserved
    byte TYPE_PASSIVE_SPAN = 4;
    byte TYPE_INDEX = 7;

    ByteBuffer encodeSpanQualifier(SpanEncodingContext<SpanBo> encodingContext);

    ByteBuffer encodeSpanColumnValue(SpanEncodingContext<SpanBo> encodingContext);


    ByteBuffer encodeSpanChunkQualifier(SpanEncodingContext<SpanChunkBo> encodingContext);

    ByteBuffer encodeSpanChunkColumnValue(SpanEncodingContext<SpanChunkBo> encodingContext);
}
