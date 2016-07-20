package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface SpanDecoder {
    
    Object UNKNOWN = new Object();

    void decode(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext, List<Object> out);

    void next(SpanDecodingContext decodingContext);

    void finish(SpanDecodingContext decodingContext);
}
