package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanBitFiled;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanEventBitField;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanEventQualifierBitField;
import com.navercorp.pinpoint.common.util.AnnotationTranscoder;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanEncoderV0 implements SpanEncoder {

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    @Override
    public ByteBuffer encodeSpanQualifier(SpanEncodingContext<SpanBo> encodingContext) {
        final SpanBo spanBo = encodingContext.getValue();
        final List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        final SpanEventBo firstEvent = getFirstSpanEvent(spanEventBoList);

        return encodeQualifier(TYPE_SPAN, spanBo.getApplicationId(), spanBo.getAgentId(), spanBo.getAgentStartTime(), spanBo.getSpanId(), firstEvent);
    }

    @Override
    public ByteBuffer encodeSpanChunkQualifier(SpanEncodingContext<SpanChunkBo> encodingContext) {
        final SpanChunkBo spanChunkBo = encodingContext.getValue();

        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        final SpanEventBo firstEvent = getFirstSpanEvent(spanEventBoList);

        return encodeQualifier(TYPE_SPAN_CHUNK, spanChunkBo.getApplicationId(), spanChunkBo.getAgentId(), spanChunkBo.getAgentStartTime(), spanChunkBo.getSpanId(), firstEvent);
    }

    private ByteBuffer encodeQualifier(byte type, String applicationId, String agentId, long agentStartTime, long spanId, SpanEventBo firstEvent) {
        final Buffer buffer = new AutomaticBuffer(128);
        buffer.putByte(type);
        buffer.putPrefixedString(applicationId);
        buffer.putPrefixedString(agentId);
        buffer.putVLong(agentStartTime);
        buffer.putLong(spanId);

        if (firstEvent != null) {
            buffer.putSVInt(firstEvent.getSequence());

            final byte bitField = SpanEventQualifierBitField.buildBitField(firstEvent);
            buffer.putByte(bitField);
            // case : async span
            if (SpanEventQualifierBitField.isSetAsync(bitField)) {
                buffer.putInt(firstEvent.getAsyncId());
                buffer.putVInt(firstEvent.getAsyncSequence());
            }
        } else {
            // simple trace case
//            buffer.putSVInt((short) -1);

//            byte cfBitField = SpanEventQualifierBitField.setAsync((byte) 0, false);
//            buffer.putByte(cfBitField);
        }

        return buffer.wrapByteBuffer();
    }



    private SpanEventBo getFirstSpanEvent(List<SpanEventBo> spanEventBoList) {
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return null;
        }

        return spanEventBoList.get(0);
    }

    @Override
    public ByteBuffer encodeSpanChunkColumnValue(SpanEncodingContext<SpanChunkBo> encodingContext) {
        final SpanChunkBo spanChunkBo = encodingContext.getValue();

        final Buffer buffer = new AutomaticBuffer(256);

        final byte version = spanChunkBo.getVersion();
        buffer.putByte(version);


        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        writeSpanEventList(buffer, spanEventBoList, encodingContext);

        return buffer.wrapByteBuffer();
    }

    private void writeSpanEventList(Buffer buffer, List<SpanEventBo> spanEventBoList, SpanEncodingContext<?> encodingContext) {
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            buffer.putVInt(0);
        } else {
            buffer.putVInt(spanEventBoList.size());

            SpanEventBo prevSpanEvent = null;
            for (SpanEventBo spanEventBo : spanEventBoList) {
                if (prevSpanEvent == null) {
                    writeFirstSpanEvent(buffer, spanEventBo, encodingContext);
                } else {
                    writeNextSpanEvent(buffer, spanEventBo, prevSpanEvent, encodingContext);
                }
                prevSpanEvent = spanEventBo;
            }
        }
    }

    @Override
    public ByteBuffer encodeSpanColumnValue(SpanEncodingContext<SpanBo> encodingContext) {
        final SpanBo span = encodingContext.getValue();

        final SpanBitFiled bitField = SpanBitFiled.build(span);

        final Buffer buffer = new AutomaticBuffer(256);

        final byte version = span.getRawVersion();
        buffer.putByte(version);

        // bit field
        buffer.putByte(bitField.getBitField());


        final short serviceType = span.getServiceType();
        buffer.putShort(serviceType);

        switch (bitField.getApplicationServiceTypeEncodingStrategy()) {
            case PREV_EQUALS:
                break;
            case RAW:
                buffer.putShort(span.getApplicationServiceType());
                break;
            default:
                throw new IllegalStateException("applicationServiceType");
        }


        // insert for rowkey
        // buffer.put(spanID);
        if (!bitField.isRoot()) {
            buffer.putLong(span.getParentSpanId());
        }

        // prevSpanEvent coding
        final long startTime = span.getStartTime();
        final long startTimeDelta = span.getCollectorAcceptTime() - startTime;
        buffer.putVLong(startTimeDelta);
        buffer.putVInt(span.getElapsed());


        buffer.putPrefixedString(span.getRpc());

        buffer.putPrefixedString(span.getEndPoint());
        buffer.putPrefixedString(span.getRemoteAddr());
        buffer.putSVInt(span.getApiId());


        // BIT flag
        if (bitField.isSetErrorCode()) {
            buffer.putInt(span.getErrCode());
        }

        if (bitField.isSetHasException()) {
            buffer.putSVInt(span.getExceptionId());
            buffer.putPrefixedString(span.getExceptionMessage());
        }

        if (bitField.isSetFlag()) {
            buffer.putShort(span.getFlag());
        }


        if (bitField.isSetLoggingTransactionInfo()) {
            buffer.putByte(span.getLoggingTransactionInfo());
        }

        buffer.putPrefixedString(span.getAcceptorHost());

        if (bitField.isSetAnnotation()) {
            List<AnnotationBo> annotationBoList = span.getAnnotationBoList();
            writeAnnotationList(buffer, annotationBoList, encodingContext);
        }

        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        writeSpanEventList(buffer, spanEventBoList, encodingContext);

        return buffer.wrapByteBuffer();
    }

    public void writeFirstSpanEvent(Buffer buffer, SpanEventBo spanEventBo, SpanEncodingContext<?> encodingContext) {

        final SpanEventBitField bitField = SpanEventBitField.buildFirst(spanEventBo);

        final byte firstSpanBitField1 = (byte) bitField.getBitField();
        buffer.putByte(firstSpanBitField1);

        buffer.putVInt(spanEventBo.getStartElapsed());
        buffer.putVInt(spanEventBo.getEndElapsed());

        buffer.putShort(spanEventBo.getSequence());
        buffer.putSVInt(spanEventBo.getDepth());
        buffer.putShort(spanEventBo.getServiceType());


        if (bitField.isSetRpc()) {
            buffer.putPrefixedString(spanEventBo.getRpc());
        }

        if (bitField.isSetEndPoint()) {
            buffer.putPrefixedString(spanEventBo.getEndPoint());
        }
        if (bitField.isSetDestinationId()) {
            buffer.putPrefixedString(spanEventBo.getDestinationId());
        }

        buffer.putSVInt(spanEventBo.getApiId());

        if (bitField.isSetNextSpanId()) {
            buffer.putLong(spanEventBo.getNextSpanId());
        }

        if (bitField.isSetHasException()) {
            buffer.putSVInt(spanEventBo.getExceptionId());
            buffer.putPrefixedString(spanEventBo.getExceptionMessage());
        }

        if (bitField.isSetAnnotation()) {
            final List<AnnotationBo> annotationBoList = spanEventBo.getAnnotationBoList();
            writeAnnotationList(buffer, annotationBoList, encodingContext);
        }

        if (bitField.isSetNextAsyncId()) {
            buffer.putSVInt(spanEventBo.getNextAsyncId());
        }

//        if (bitField.isSetAsyncId()) {
//            buffer.putInt(spanEventBo.getAsyncId());
//            buffer.putVInt(spanEventBo.getAsyncSequence());
//        }
    }

    public void writeNextSpanEvent(Buffer buffer, SpanEventBo spanEventBo, SpanEventBo prevSpanEvent, SpanEncodingContext<?> encodingContext) {

        final SpanEventBitField bitField = SpanEventBitField.build(spanEventBo, prevSpanEvent);

        buffer.putShort(bitField.getBitField());

        switch (bitField.getStartElapsedEncodingStrategy()) {
            case PREV_DELTA:
                final int startTimeDelta = spanEventBo.getStartElapsed() - prevSpanEvent.getStartElapsed();
                buffer.putVInt(startTimeDelta);
                break;
            case PREV_EQUALS:
                // skip bitfield
                break;
            default:
                throw new IllegalStateException("unsupported StartElapsedEncodingStrategy");
        }
        buffer.putVInt(spanEventBo.getEndElapsed());

        switch (bitField.getSequenceEncodingStrategy()) {
            case PREV_DELTA:
                final int sequenceDelta = spanEventBo.getSequence() - prevSpanEvent.getSequence();
                buffer.putVInt(sequenceDelta);
                break;
            case PREV_ADD1:
                // skip bitfield
                break;
            default:
                throw new IllegalStateException("unsupported SequenceEncodingStrategy");
        }

        switch (bitField.getDepthEncodingStrategy()) {
            case RAW:
                buffer.putSVInt(spanEventBo.getDepth());
                break;
            case PREV_EQUALS:
                // skip bitfield
                break;
            default:
                throw new IllegalStateException("unsupported DepthEncodingStrategy");
        }

        switch (bitField.getServiceTypeEncodingStrategy()) {
            case RAW:
                buffer.putShort(spanEventBo.getServiceType());
                break;
            case PREV_EQUALS:
                // skip bitfield
                break;
            default:
                throw new IllegalStateException("unsupported ServiceTypeEncodingStrategy");
        }


        buffer.putSVInt(spanEventBo.getApiId());

        if (bitField.isSetRpc()) {
            buffer.putPrefixedString(spanEventBo.getRpc());
        }

        if (bitField.isSetEndPoint()) {
            buffer.putPrefixedString(spanEventBo.getEndPoint());
        }
        if (bitField.isSetDestinationId()) {
            buffer.putPrefixedString(spanEventBo.getDestinationId());
        }

        if (bitField.isSetNextSpanId()) {
            buffer.putLong(spanEventBo.getNextSpanId());
        }

        if (bitField.isSetHasException()) {
            buffer.putSVInt(spanEventBo.getExceptionId());
            buffer.putPrefixedString(spanEventBo.getExceptionMessage());
        }

        if (bitField.isSetAnnotation()) {
            List<AnnotationBo> annotationBoList = spanEventBo.getAnnotationBoList();
            writeAnnotationList(buffer, annotationBoList, encodingContext);
        }

        if (bitField.isSetNextAsyncId()) {
            buffer.putSVInt(spanEventBo.getNextAsyncId());
        }

        if (bitField.isSetAsyncId()) {
            buffer.putInt(spanEventBo.getAsyncId());
            buffer.putVInt(spanEventBo.getAsyncSequence());
        }
    }

    private void writeAnnotationList(Buffer buffer, List<AnnotationBo> annotationBoList, SpanEncodingContext<?> encodingContext) {
        if (CollectionUtils.isEmpty(annotationBoList)) {
            return;
        }

        buffer.putVInt(annotationBoList.size());

//        AnnotationBo prev = encodingCtx.getPrevFirstAnnotationBo();
        AnnotationBo prev = null;
        for (int i = 0; i < annotationBoList.size(); i++) {
            final AnnotationBo current = annotationBoList.get(i);
            // first row
            if (i == 0) {

                // first annotation
                buffer.putSVInt(current.getKey());

                Object value = current.getValue();
                byte valueTypeCode = transcoder.getTypeCode(value);
                byte[] valueBytes = transcoder.encode(value, valueTypeCode);

                buffer.putByte(valueTypeCode);
                buffer.putPrefixedBytes(valueBytes);
//                else {
//                    writeDeltaAnnotationBo(buffer, prev, current);
//                }
                // save first annotation
//                encodingCtx.setPrevFirstAnnotationBo(current);
            } else {
                writeDeltaAnnotationBo(buffer, prev, current);
            }
            prev = current;
        }
    }

    private void writeDeltaAnnotationBo(Buffer buffer, AnnotationBo prev, AnnotationBo current) {
        // prev : -30 cur: -20  = -20 - - 30 = 10
        // prev :  20 cur: 100  =  100 - 20 = 80
        // prev :  -40 cur: 1000  =  1000 + 40 = 10040
        final int prevKey = prev.getKey();
        final int currentKey = current.getKey();
        buffer.putSVInt(currentKey - prevKey);

        Object value = current.getValue();
        byte valueTypeCode = transcoder.getTypeCode(value);
        byte[] valueBytes = transcoder.encode(value, valueTypeCode);

        buffer.putByte(valueTypeCode);
        buffer.putPrefixedBytes(valueBytes);
    }


}
