package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACES_CF_TERMINALSPAN;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanEventSerializer implements HbaseSerializer<SpanEventEncodingContext, Put> {

    private AnnotationSerializer annotationSerializer;

    @Autowired
    public void setAnnotationSerializer(AnnotationSerializer annotationSerializer) {
        this.annotationSerializer = annotationSerializer;
    }

    @Override
    public void serialize(SpanEventEncodingContext spanEventEncodingContext, Put put, SerializationContext context) {

        ByteBuffer rowId = writeQualifier(spanEventEncodingContext);

        final ByteBuffer value = writeValue(spanEventEncodingContext);

        final long acceptedTime = put.getTimeStamp();

        put.addColumn(TRACES_CF_TERMINALSPAN, rowId, acceptedTime, value);

    }

    private ByteBuffer writeQualifier(SpanEventEncodingContext spanEventEncodingContext) {
        SpanEventBo spanEventBo = spanEventEncodingContext.getSpanEventBo();
        BasicSpan basicSpan = spanEventEncodingContext.getBasicSpan();

        final Buffer rowId = new AutomaticBuffer();
        rowId.putLong(basicSpan.getSpanId());
        rowId.putShort(spanEventBo.getSequence());
        rowId.putInt(spanEventBo.getAsyncId());
        rowId.putShort(spanEventBo.getAsyncSequence());
        return rowId.wrapByteBuffer();
    }

    public ByteBuffer writeValue(SpanEventEncodingContext spanEventEncodingContext) {
        SpanEventBo spanEventBo = spanEventEncodingContext.getSpanEventBo();
        BasicSpan basicSpan = spanEventEncodingContext.getBasicSpan();

        final Buffer buffer = new AutomaticBuffer(512);

        buffer.putByte(spanEventBo.getVersion());

        buffer.putPrefixedString(basicSpan.getAgentId());
        buffer.putPrefixedString(basicSpan.getApplicationId());
        buffer.putVLong(basicSpan.getAgentStartTime());

        buffer.putVInt(spanEventBo.getStartElapsed());
        buffer.putVInt(spanEventBo.getEndElapsed());

        // don't need to put sequence because it is set at Qualifier
        // buffer.put(sequence);

        buffer.putPrefixedString(spanEventBo.getRpc());
        buffer.putShort(spanEventBo.getServiceType());
        buffer.putPrefixedString(spanEventBo.getEndPoint());
        buffer.putPrefixedString(spanEventBo.getDestinationId());
        buffer.putSVInt(spanEventBo.getApiId());

        buffer.putSVInt(spanEventBo.getDepth());
        buffer.putLong(spanEventBo.getNextSpanId());

        if (spanEventBo.hasException()) {
            buffer.putBoolean(true);
            buffer.putSVInt(spanEventBo.getExceptionId());
            buffer.putPrefixedString(spanEventBo.getExceptionMessage());
        } else {
            buffer.putBoolean(false);
        }
        final List<AnnotationBo> annotationBoList = spanEventBo.getAnnotationBoList();
        this.annotationSerializer.writeAnnotationList(annotationBoList, buffer);

        buffer.putSVInt(spanEventBo.getNextAsyncId());

        return buffer.wrapByteBuffer();
    }

}
