package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACES_CF_SPAN;
/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanSerializer implements HbaseSerializer<SpanBo, Put> {

    @Override
    public void serialize(SpanBo spanBo, Put put, SerializationContext context) {

        ByteBuffer columnValue = writeColumnValue(spanBo);

        // TODO  if we can identify whether the columnName is duplicated or not,
        // we can also know whether the span id is duplicated or not.
        ByteBuffer spanId = ByteBuffer.wrap(Bytes.toBytes(spanBo.getSpanId()));

        long acceptedTime = put.getTimeStamp();
        put.addColumn(TRACES_CF_SPAN, spanId, acceptedTime, columnValue);

    }

    // Variable encoding has been added in case of write io operation. The data size can be reduced by about 10%.
    public ByteBuffer writeColumnValue(SpanBo span) {
        /*
           It is difficult to calculate the size of buffer. It's not impossible.
           However just use automatic incremental buffer for convenience's sake.
           Consider to reuse getBufferLength when memory can be used more efficiently later.
        */
        final Buffer buffer = new AutomaticBuffer(256);

        buffer.putByte(span.getRawVersion());


        buffer.putPrefixedString(span.getAgentId());

        // Using var makes the sie of time smaller based on the present time. That consumes only 6 bytes.
        buffer.putVLong(span.getAgentStartTime());

        // insert for rowkey
        // buffer.put(spanID);
        buffer.putLong(span.getParentSpanId());

        // use var encoding because of based on the present time
        buffer.putVLong(span.getStartTime());
        buffer.putVInt(span.getElapsed());

        buffer.putPrefixedString(span.getRpc());
        buffer.putPrefixedString(span.getApplicationId());
        buffer.putShort(span.getServiceType());
        buffer.putPrefixedString(span.getEndPoint());
        buffer.putPrefixedString(span.getRemoteAddr());
        buffer.putSVInt(span.getApiId());

        // errCode value may be negative
        buffer.putSVInt(span.getErrCode());

        if (span.hasException()){
            buffer.putBoolean(true);
            buffer.putSVInt(span.getExceptionId());
            buffer.putPrefixedString(span.getExceptionMessage());
        } else {
            buffer.putBoolean(false);
        }

        buffer.putShort(span.getFlag());

        if (span.hasApplicationServiceType()) {
            buffer.putBoolean(true);
            buffer.putShort(span.getApplicationServiceType());
        } else {
            buffer.putBoolean(false);
        }

        buffer.putByte(span.getLoggingTransactionInfo());
        buffer.putPrefixedString(span.getAcceptorHost());

        return buffer.wrapByteBuffer();
    }
}
