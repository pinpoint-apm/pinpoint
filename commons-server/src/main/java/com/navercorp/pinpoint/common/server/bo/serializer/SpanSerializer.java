package com.navercorp.pinpoint.common.server.bo.serializer;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACES_CF_SPAN;
/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanSerializer implements HbaseSerializer<SpanBo, Put> {

    private AcceptedTimeService acceptedTimeService;

    @Autowired
    public void setAcceptedTimeService(AcceptedTimeService acceptedTimeService) {
        this.acceptedTimeService = acceptedTimeService;
    }

    @Override
    public void serialize(SpanBo spanBo, Put put, SerializationContext context) {

        byte[] columnValue = writeColumnValue(spanBo);

        // TODO  if we can identify whether the columnName is duplicated or not,
        // we can also know whether the span id is duplicated or not.
        byte[] spanId = Bytes.toBytes(spanBo.getSpanId());

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        put.addColumn(TRACES_CF_SPAN, spanId, acceptedTime, columnValue);

    }

    // Variable encoding has been added in case of write io operation. The data size can be reduced by about 10%.
    public byte[] writeColumnValue(SpanBo span) {
        /*
           It is difficult to calculate the size of buffer. It's not impossible.
           However just use automatic incremental buffer for convenience's sake.
           Consider to reuse getBufferLength when memory can be used more efficiently later.
        */
        final Buffer buffer = new AutomaticBuffer(256);

        buffer.put(span.getRawVersion());


        buffer.putPrefixedString(span.getAgentId());

        // Using var makes the sie of time smaller based on the present time. That consumes only 6 bytes.
        buffer.putVar(span.getAgentStartTime());

        // insert for rowkey
        // buffer.put(spanID);
        buffer.put(span.getParentSpanId());

        // use var encoding because of based on the present time
        buffer.putVar(span.getStartTime());
        buffer.putVar(span.getElapsed());

        buffer.putPrefixedString(span.getRpc());
        buffer.putPrefixedString(span.getApplicationId());
        buffer.put(span.getServiceType());
        buffer.putPrefixedString(span.getEndPoint());
        buffer.putPrefixedString(span.getRemoteAddr());
        buffer.putSVar(span.getApiId());

        // errCode value may be negative
        buffer.putSVar(span.getErrCode());

        if (span.hasException()){
            buffer.put(true);
            buffer.putSVar(span.getExceptionId());
            buffer.putPrefixedString(span.getExceptionMessage());
        } else {
            buffer.put(false);
        }

        buffer.put(span.getFlag());

        if (span.hasApplicationServiceType()) {
            buffer.put(true);
            buffer.put(span.getApplicationServiceType());
        } else {
            buffer.put(false);
        }

        buffer.put(span.getLoggingTransactionInfo());
        buffer.putPrefixedString(span.getAcceptorHost());

        return buffer.getBuffer();
    }
}
