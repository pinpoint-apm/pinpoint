package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;


/**
 * @author Woonduk Kang(emeroad)
 */
public interface SpanDecoder {
    
    Object UNKNOWN = new Object();

    Object decode(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext);

    void next(SpanDecodingContext decodingContext);

}
