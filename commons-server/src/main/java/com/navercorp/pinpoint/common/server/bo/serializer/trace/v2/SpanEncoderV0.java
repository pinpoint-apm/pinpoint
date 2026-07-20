package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationTranscoder;
import com.navercorp.pinpoint.common.server.bo.AttributeBo;
import com.navercorp.pinpoint.common.server.bo.AttributeTranscoder;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanBitField;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanEventBitField;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanEventQualifierBitField;
import com.navercorp.pinpoint.io.SpanVersion;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanEncoderV0 implements SpanEncoder {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();
    private static final AttributeTranscoder attributeTranscoder = new AttributeTranscoder();

    @Override
    public ByteBuffer encodeSpanQualifier(SpanEncodingContext<SpanBo> encodingContext) {
        final SpanBo spanBo = encodingContext.getValue();
        final List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
        final SpanEventBo firstEvent = getFirstSpanEvent(spanEventBoList);

        final SpanHeader header = SpanHeader.span(spanBo.getTraceSourceType());
        return encodeQualifier(header, spanBo, firstEvent, null);
    }

    @Override
    public ByteBuffer encodeSpanChunkQualifier(SpanEncodingContext<SpanChunkBo> encodingContext) {
        final SpanChunkBo spanChunkBo = encodingContext.getValue();

        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        final SpanEventBo firstEvent = getFirstSpanEvent(spanEventBoList);

        LocalAsyncIdBo localAsyncId = spanChunkBo.getLocalAsyncId();
        final SpanHeader header = SpanHeader.spanChunk(spanChunkBo.getTraceSourceType());
        return encodeQualifier(header, spanChunkBo, firstEvent, localAsyncId);
    }

    private ByteBuffer encodeQualifier(SpanHeader header, BasicSpan basicSpan, SpanEventBo firstEvent, LocalAsyncIdBo localAsyncId) {
        final SpanOwner owner = basicSpan.getSpanOwner();
        final Buffer buffer = new AutomaticBuffer(128);
        buffer.putByte(header.getCode());
        buffer.putPrefixedString(owner.getApplicationName());
        buffer.putPrefixedString(owner.getAgentId());
        buffer.putVLong(owner.getAgentStartTime());
        buffer.putLong(basicSpan.getSpanId());

        if (firstEvent != null) {
            buffer.putSVInt(firstEvent.getSequence());

            final byte bitField = SpanEventQualifierBitField.buildBitField(localAsyncId);
            buffer.putByte(bitField);
            // case : async span
            if (SpanEventQualifierBitField.isSetAsync(bitField)) {
                buffer.putInt(localAsyncId.getAsyncId());
                buffer.putVInt(localAsyncId.getSequence());
            }
        }
//        else {
            // simple trace case
//            buffer.putSVInt((short) -1);

//            byte cfBitField = SpanEventQualifierBitField.setAsync((byte) 0, false);
//            buffer.putByte(cfBitField);
//        }

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

        final byte version = (byte) spanChunkBo.getVersion();
        buffer.putByte(version);
        long spanEventBaseTimeNanos = 0;
        if (version == SpanVersion.TRACE_V3) {
            // keyTime is stored as an absolute epoch-nanos value (not a collectorAcceptTime delta),
            // so it survives HBase cell-timestamp drift the same way TRACE_V2 keyTime does.
            final long keyTimeNanos = spanChunkBo.getKeyTimeNanos();
            buffer.putVLong(keyTimeNanos);
            spanEventBaseTimeNanos = keyTimeNanos;
        } else if (version == SpanVersion.TRACE_V2) {
            buffer.putVLong(spanChunkBo.getKeyTimeMillis());
        }

        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
        writeSpanEventList(buffer, spanEventBoList, encodingContext, version, spanEventBaseTimeNanos);

        return buffer.wrapByteBuffer();
    }

    private void writeSpanEventList(Buffer buffer, List<SpanEventBo> spanEventBoList, SpanEncodingContext<?> encodingContext, byte version, long baseTimeNanos) {
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            buffer.putVInt(0);
        } else {
            buffer.putVInt(spanEventBoList.size());

            SpanEventBo prevSpanEvent = null;
            for (SpanEventBo spanEventBo : spanEventBoList) {
                if (version == SpanVersion.TRACE_V3) {
                    writeSpanEventTime(buffer, spanEventBo, baseTimeNanos);
                }
                if (prevSpanEvent == null) {
                    writeFirstSpanEvent(buffer, spanEventBo, encodingContext);
                } else {
                    writeNextSpanEvent(buffer, spanEventBo, prevSpanEvent, encodingContext);
                }
                prevSpanEvent = spanEventBo;
            }
        }
    }

    private void writeSpanEventTime(Buffer buffer, SpanEventBo spanEventBo, long baseTimeNanos) {
        final long startTime = spanEventBo.getStartTimeNanos();
        final long endTime = spanEventBo.getEndTimeNanos();
        buffer.putSVLong(startTime - baseTimeNanos);
        buffer.putVLong(Math.max(endTime - startTime, 0));
    }

    @Override
    public ByteBuffer encodeSpanColumnValue(SpanEncodingContext<SpanBo> encodingContext) {
        final SpanBo span = encodingContext.getValue();

        final SpanBitField bitField = SpanBitField.build(span);

        final Buffer buffer = new AutomaticBuffer(256);

        final byte version = span.getRawVersion();
        buffer.putByte(version);

        // bit field
        buffer.putByte(bitField.getBitField());


        final short serviceType = (short) span.getServiceType();
        buffer.putShort(serviceType);

        switch (bitField.getApplicationServiceTypeEncodingStrategy()) {
            case PREV_EQUALS:
                break;
            case RAW:
                buffer.putShort((short) span.getApplicationServiceType());
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
        long spanEventBaseTimeNanos = 0;
        if (version == SpanVersion.TRACE_V3) {
            if (!span.hasEndTime()) {
                throw new IllegalStateException("span end time is not set");
            }
            final long startTime = span.getStartTimeNanos();
            final long collectorAcceptTimeNanos = TimeUnit.MILLISECONDS.toNanos(span.getCollectorAcceptTime());
            buffer.putSVLong(collectorAcceptTimeNanos - startTime);
            final long endTime = span.getEndTimeNanos();
            buffer.putVLong(Math.max(endTime - startTime, 0));
            spanEventBaseTimeNanos = startTime;
        } else {
            final long startTime = span.getStartTimeMillis();
            final long startTimeDelta = span.getCollectorAcceptTime() - startTime;
            buffer.putVLong(startTimeDelta);
        }
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
            if (span.hasException()) {
                ExceptionInfo exceptionInfo = span.getExceptionInfo();
                buffer.putSVInt(exceptionInfo.id());
                buffer.putPrefixedString(exceptionInfo.message());
            }
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

        if (bitField.isSetAttribute()) {
            List<AttributeBo> attributeBoList = span.getAttributeBoList();
            attributeTranscoder.writeAttributeList(buffer, attributeBoList);
        }

        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        writeSpanEventList(buffer, spanEventBoList, encodingContext, version, spanEventBaseTimeNanos);

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
        buffer.putShort((short) spanEventBo.getServiceType());

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
            ExceptionInfo exceptionInfo = spanEventBo.getExceptionInfo();
            if (exceptionInfo!= null) {
                buffer.putSVInt(exceptionInfo.id());
                buffer.putPrefixedString(exceptionInfo.message());
            }
        }

        if (bitField.isSetAnnotation()) {
            final List<AnnotationBo> annotationBoList = spanEventBo.getAnnotationBoList();
            writeAnnotationList(buffer, annotationBoList, encodingContext);
        }

        if (bitField.isSetNextAsyncId()) {
            buffer.putSVInt(spanEventBo.getNextAsyncId());
        }

        if (bitField.isSetAttribute()) {
            List<AttributeBo> attributeBoList = spanEventBo.getAttributeBoList();
            attributeTranscoder.writeAttributeList(buffer, attributeBoList);
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
                buffer.putShort((short) spanEventBo.getServiceType());
                break;
            case PREV_EQUALS:
                // skip bitfield
                break;
            default:
                throw new IllegalStateException("unsupported ServiceTypeEncodingStrategy");
        }


        buffer.putSVInt(spanEventBo.getApiId());

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
            ExceptionInfo exceptionInfo = spanEventBo.getExceptionInfo();
            if (exceptionInfo != null) {
                buffer.putSVInt(exceptionInfo.id());
                buffer.putPrefixedString(exceptionInfo.message());
            }
        }

        if (bitField.isSetAnnotation()) {
            List<AnnotationBo> annotationBoList = spanEventBo.getAnnotationBoList();
            writeAnnotationList(buffer, annotationBoList, encodingContext);
        }

        if (bitField.isSetNextAsyncId()) {
            buffer.putSVInt(spanEventBo.getNextAsyncId());
        }

        if (bitField.isSetAttribute()) {
            List<AttributeBo> attributeBoList = spanEventBo.getAttributeBoList();
            attributeTranscoder.writeAttributeList(buffer, attributeBoList);
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

                writeAnnotationValue(buffer, current.getValue());
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

        writeAnnotationValue(buffer, current.getValue());
    }

    private void writeAnnotationValue(Buffer buffer, Object value) {
        final AnnotationTranscoder.ValueEncoder valueEncoder = transcoder.getEncoder(value);
        valueEncoder.encode(buffer, value);
    }


}
