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
import org.apache.commons.collections.CollectionUtils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEncoder {

    public static final Comparator<SpanEventBo> SPAN_EVENT_SEQUENCE_COMPARATOR = new Comparator<SpanEventBo>() {
        @Override
        public int compare(SpanEventBo o1, SpanEventBo o2) {
            final int sequenceCompare = Short.compare(o1.getSequence(), o2.getSequence());
            if (sequenceCompare != 0) {
                return sequenceCompare;
            }
            final int asyncId1 = o1.getAsyncId();
            final int asyncId2 = o2.getAsyncId();
            final int asyncIdCompare = Integer.compare(asyncId1, asyncId2);
            if (asyncIdCompare != 0) {
//                bug Comparison method violates its general contract!
//                TODO temporary fix
//                if (asyncId1 == -1) {
//                    return -1;
//                }
//                if (asyncId2 == -1) {
//                    return -1;
//                }
                return asyncIdCompare;
            }
            return Integer.compare(o1.getAsyncSequence(), o2.getAsyncSequence());
        }
    };

    public static final Comparator<AnnotationBo> ANNOTATION_COMPARATOR = new Comparator<AnnotationBo>() {
        @Override
        public int compare(AnnotationBo o1, AnnotationBo o2) {
            return Integer.compare(o1.getKey(), o2.getKey());
        }
    };
    public static byte TYPE_SPAN = 0;
    public static byte TYPE_SPAN_CHUNK = 1;

    // reserved
    public static byte TYPE_INDEX = 2;


    public ByteBuffer encodeSpanQualifier(SpanEncodingContext<SpanBo> encodingCtx) {
        final SpanBo spanBo = encodingCtx.getValue();
        final List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        final SpanEventBo firstEvent = getFirstSpanEvent(spanEventBoList);

        return encodeQualifier(TYPE_SPAN, spanBo.getApplicationId(), spanBo.getAgentId(), spanBo.getAgentStartTime(), spanBo.getSpanId(), firstEvent);
    }

    public ByteBuffer encodeSpanChunkQualifier(SpanEncodingContext<SpanChunkBo> encodingCtx) {
        final SpanChunkBo spanChunkBo = encodingCtx.getValue();

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
        // TODO duplicated sort
        sortSpanEvent(spanEventBoList);
        return spanEventBoList.get(0);
    }

    public ByteBuffer encodeSpanChunkColumnValue(SpanEncodingContext<SpanChunkBo> encodingCtx) {
        final SpanChunkBo spanChunkBo = encodingCtx.getValue();
        // TODO duplicated sort
        sortSpanEvent(spanChunkBo.getSpanEventBoList());

        final Buffer buffer = new AutomaticBuffer(256);

        final byte version = spanChunkBo.getVersion();
        buffer.putByte(version);


        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        writeSpanEventList(buffer, spanEventBoList, encodingCtx);

        return buffer.wrapByteBuffer();
    }

    private void writeSpanEventList(Buffer buffer, List<SpanEventBo> spanEventBoList, SpanEncodingContext<?> encodingCtx) {
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            buffer.putVInt(0);
        } else {
            buffer.putVInt(spanEventBoList.size());

            SpanEventBo prevSpanEvent = null;
            for (SpanEventBo spanEventBo : spanEventBoList) {
                if (prevSpanEvent == null) {
                    writeFirstSpanEvent(buffer, spanEventBo, encodingCtx);
                } else {
                    writeNextSpanEvent(buffer, spanEventBo, prevSpanEvent, encodingCtx);
                }
                prevSpanEvent = spanEventBo;
            }
        }
    }

    public ByteBuffer encodeSpanColumnValue(SpanEncodingContext<SpanBo> encodingCtx) {
        final SpanBo span = encodingCtx.getValue();

        sortSpanEvent(span.getSpanEventBoList());

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
            writeAnnotationList(buffer, annotationBoList, encodingCtx);
        }

        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        writeSpanEventList(buffer, spanEventBoList, encodingCtx);

        return buffer.wrapByteBuffer();
    }

    private void sortSpanEvent(List<SpanEventBo> spanEventBoList) {

        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }
        Collections.sort(spanEventBoList, SPAN_EVENT_SEQUENCE_COMPARATOR);
    }

    public void writeFirstSpanEvent(Buffer buffer, SpanEventBo spanEventBo, SpanEncodingContext<?> encodingCtx) {

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
            writeAnnotationList(buffer, annotationBoList, encodingCtx);
        }

        if (bitField.isSetNextAsyncId()) {
            buffer.putSVInt(spanEventBo.getNextAsyncId());
        }

//        if (bitField.isSetAsyncId()) {
//            buffer.putInt(spanEventBo.getAsyncId());
//            buffer.putVInt(spanEventBo.getAsyncSequence());
//        }
    }

    public void writeNextSpanEvent(Buffer buffer, SpanEventBo spanEventBo, SpanEventBo prevSpanEvent, SpanEncodingContext<?> encodingCtx) {

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
                throw new IllegalStateException("unsupported SequenceEncodingStrategy");
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
            writeAnnotationList(buffer, annotationBoList, encodingCtx);
        }

        if (bitField.isSetNextAsyncId()) {
            buffer.putSVInt(spanEventBo.getNextAsyncId());
        }

        if (bitField.isSetAsyncId()) {
            buffer.putInt(spanEventBo.getAsyncId());
            buffer.putVInt(spanEventBo.getAsyncSequence());
        }
    }

    private void writeAnnotationList(Buffer buffer, List<AnnotationBo> annotationBoList, SpanEncodingContext<?> encodingCtx) {
        if (CollectionUtils.isEmpty(annotationBoList)) {
            return;
        }
        Collections.sort(annotationBoList, ANNOTATION_COMPARATOR);

        buffer.putVInt(annotationBoList.size());

//        AnnotationBo prev = encodingCtx.getPrevFirstAnnotationBo();
        AnnotationBo prev = null;
        for (int i = 0; i < annotationBoList.size(); i++) {
            final AnnotationBo current = annotationBoList.get(i);
            // first row
            if (i == 0) {

                // first annotation
                buffer.putSVInt(current.getKey());
                buffer.putByte(current.getRawValueType());
                buffer.putPrefixedBytes(current.getByteValue());
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
        buffer.putByte(current.getRawValueType());
        buffer.putPrefixedBytes(current.getByteValue());
    }


}
