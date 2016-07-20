package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanBitFiled;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanEventBitField;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.bitfield.SpanEventQualifierBitField;
import com.navercorp.pinpoint.common.util.AnnotationTranscoder;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanDecoderV0 implements SpanDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    @Override
    public void decode(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext, List<Object> out) {
        final byte type = qualifier.readByte();

        if (SpanEncoder.TYPE_SPAN == type) {

            SpanBo span = readSpan(qualifier, columnValue, decodingContext);
            out.add(span);

        } else if (SpanEncoder.TYPE_SPAN_CHUNK == type) {

            SpanChunkBo spanChunk = readSpanChunk(qualifier, columnValue, decodingContext);
            out.add(spanChunk);

        } else {
            logger.warn("Unknown span type {}", type);
            out.add(UNKNOWN);
        }
    }

    private SpanChunkBo readSpanChunk(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext) {
        final SpanChunkBo spanChunk = new SpanChunkBo();

        final TransactionId transactionId = decodingContext.getTransactionId();

        spanChunk.setTraceAgentStartTime(transactionId.getAgentStartTime());
        spanChunk.setTraceTransactionSequence(transactionId.getTransactionSequence());
        spanChunk.setCollectorAcceptTime(decodingContext.getCollectorAcceptedTime());


        SpanAdaptor spanAdaptor = new SpanChunkBoAdaptor(spanChunk);
        SpanEventBo firstSpanEvent = readQualifier(spanAdaptor, qualifier);

        readSpanChunkValue(columnValue, spanChunk, firstSpanEvent, decodingContext);

        return spanChunk;
    }


    private SpanBo readSpan(Buffer qualifier, Buffer columnValue, SpanDecodingContext decodingContext) {
        final SpanBo span = new SpanBo();

        final TransactionId transactionId = decodingContext.getTransactionId();
        span.setTraceAgentId(transactionId.getAgentId());
        span.setTraceAgentStartTime(transactionId.getAgentStartTime());
        span.setTraceTransactionSequence(transactionId.getTransactionSequence());
        span.setCollectorAcceptTime(decodingContext.getCollectorAcceptedTime());

        SpanAdaptor spanAdaptor = new SpanBoAdaptor(span);
        SpanEventBo firstSpanEvent = readQualifier(spanAdaptor, qualifier);

        readSpanValue(columnValue, span, firstSpanEvent, decodingContext);

        return span;
    }

    private void readSpanChunkValue(Buffer buffer, SpanChunkBo spanChunk, SpanEventBo firstSpanEvent, SpanDecodingContext decodingContext) {
        final byte version = buffer.readByte();
        if (version != 0) {
            throw new IllegalStateException("unknown version :" + version);
        }
        spanChunk.setVersion(version);

        List<SpanEventBo> spanEventBoList = readSpanEvent(buffer, firstSpanEvent, decodingContext);
        spanChunk.addSpanEventBoList(spanEventBoList);
    }

    public void readSpanValue(Buffer buffer, SpanBo span, SpanEventBo firstSpanEvent, SpanDecodingContext decodingContext) {

        final byte version = buffer.readByte();
        if (version != 0) {
            throw new IllegalStateException("unknown version :" + version);
        }
        span.setVersion(version);

        final SpanBitFiled bitFiled = new SpanBitFiled(buffer.readByte());

        final short serviceType = buffer.readShort();
        span.setServiceType(serviceType);

        switch (bitFiled.getApplicationServiceTypeEncodingStrategy()) {
            case PREV_EQUALS:
                span.setApplicationServiceType(serviceType);
                break;
            case RAW:
                span.setApplicationServiceType(buffer.readShort());
                break;
            default:
                throw new IllegalStateException("applicationServiceType");
        }

        if (!bitFiled.isRoot()) {
            span.setParentSpanId(buffer.readLong());
        } else {
            span.setParentSpanId(-1);
        }

        final int startTimeDelta = buffer.readVInt();
        final long startTime = startTimeDelta + span.getCollectorAcceptTime();
        span.setStartTime(startTime);
        span.setElapsed(buffer.readVInt());

        span.setRpc(buffer.readPrefixedString());

        span.setEndPoint(buffer.readPrefixedString());
        span.setRemoteAddr(buffer.readPrefixedString());
        span.setApiId(buffer.readSVInt());

        if (bitFiled.isSetErrorCode()) {
            span.setErrCode(buffer.readInt());
        }
        if (bitFiled.isSetHasException()) {
            int exceptionId = buffer.readSVInt();
            String exceptionMessage = buffer.readPrefixedString();
            span.setExceptionInfo(exceptionId, exceptionMessage);
        }

        if (bitFiled.isSetFlag()) {
            span.setFlag(buffer.readShort());
        }

        if (bitFiled.isSetLoggingTransactionInfo()) {
            span.setLoggingTransactionInfo(buffer.readByte());
        }

        span.setAcceptorHost(buffer.readPrefixedString());


        if (bitFiled.isSetAnnotation()) {
            List<AnnotationBo> annotationBoList = readAnnotationList(buffer, decodingContext);
            span.setAnnotationBoList(annotationBoList);
        }

        List<SpanEventBo> spanEventBoList = readSpanEvent(buffer, firstSpanEvent, decodingContext);
        span.addSpanEventBoList(spanEventBoList);


    }

    private List<SpanEventBo> readSpanEvent(Buffer buffer, SpanEventBo firstSpanEvent, SpanDecodingContext decodingContext) {
        final int spanEventSize = buffer.readVInt();
        if (spanEventSize <= 0) {
            return new ArrayList<>();
        }
        final List<SpanEventBo> spanEventBoList = new ArrayList<>();
        SpanEventBo prev = null;
        for (int i = 0; i < spanEventSize; i++) {
            SpanEventBo spanEvent;
            if (i == 0) {
                spanEvent = readFirstSpanEvent(buffer, firstSpanEvent, decodingContext);
            } else {
                spanEvent = readNextSpanEvent(buffer, prev, decodingContext);
            }
            prev = spanEvent;
            spanEventBoList.add(spanEvent);
        }

        return spanEventBoList;
    }

    private SpanEventBo readNextSpanEvent(final Buffer buffer, final SpanEventBo prev, SpanDecodingContext decodingContext) {
        final SpanEventBo spanEventBo = new SpanEventBo();

        final SpanEventBitField bitField = new SpanEventBitField(buffer.readShort());

        switch (bitField.getStartElapsedEncodingStrategy()) {
            case PREV_DELTA:
                int startTimeDelta = buffer.readVInt();
                int startTime = startTimeDelta + prev.getStartElapsed();
                spanEventBo.setStartElapsed(startTime);
                break;
            case PREV_EQUALS:
                spanEventBo.setStartElapsed(prev.getStartElapsed());
                break;
            default:
                throw new IllegalStateException("unsupported SequenceEncodingStrategy");
        }
        spanEventBo.setEndElapsed(buffer.readVInt());

        switch (bitField.getSequenceEncodingStrategy()) {
            case PREV_DELTA:
                int sequenceDelta = buffer.readVInt();
                final int sequence = sequenceDelta + prev.getSequence();
                spanEventBo.setSequence((short) sequence);
                break;
            case PREV_ADD1:
                spanEventBo.setSequence((short) (prev.getSequence() + 1));
                break;
            default:
                throw new IllegalStateException("unsupported SequenceEncodingStrategy");
        }

        switch (bitField.getDepthEncodingStrategy()) {
            case RAW:
                spanEventBo.setDepth(buffer.readSVInt());
                break;
            case PREV_EQUALS:
                spanEventBo.setDepth(prev.getDepth());
                break;
            default:
                throw new IllegalStateException("unsupported DepthEncodingStrategy");
        }

        switch (bitField.getServiceTypeEncodingStrategy()) {
            case RAW:
                spanEventBo.setServiceType(buffer.readShort());
                break;
            case PREV_EQUALS:
                spanEventBo.setServiceType(prev.getServiceType());
                break;
            default:
                throw new IllegalStateException("unsupported ServiceTypeEncodingStrategy");
        }


        spanEventBo.setApiId(buffer.readSVInt());

        if (bitField.isSetRpc()) {
            spanEventBo.setRpc(buffer.readPrefixedString());
        }

        if (bitField.isSetEndPoint()) {
            spanEventBo.setEndPoint(buffer.readPrefixedString());
        }
        if (bitField.isSetDestinationId()) {
            spanEventBo.setDestinationId(buffer.readPrefixedString());
        }

        if (bitField.isSetNextSpanId()) {
            spanEventBo.setNextSpanId(buffer.readLong());
        }


        if (bitField.isSetHasException()) {
            int exceptionId = buffer.readSVInt();
            String exceptionMessage = buffer.readPrefixedString();
            spanEventBo.setExceptionInfo(exceptionId, exceptionMessage);
        }

        if (bitField.isSetAnnotation()) {
            List<AnnotationBo> annotationBoList = readAnnotationList(buffer, decodingContext);
            spanEventBo.setAnnotationBoList(annotationBoList);
        }

        if (bitField.isSetNextAsyncId()) {
            spanEventBo.setNextAsyncId(buffer.readSVInt());
        }

        if (bitField.isSetAsyncId()) {
            spanEventBo.setAsyncId(buffer.readInt());
            spanEventBo.setAsyncSequence((short) buffer.readVInt());
        }

        return spanEventBo;
    }

    private SpanEventBo readFirstSpanEvent(Buffer buffer, SpanEventBo firstSpanEvent, SpanDecodingContext decodingContext) {
        SpanEventBitField bitField = new SpanEventBitField(buffer.readByte());

        firstSpanEvent.setStartElapsed(buffer.readVInt());
        firstSpanEvent.setEndElapsed(buffer.readVInt());

        firstSpanEvent.setSequence(buffer.readShort());
        firstSpanEvent.setDepth(buffer.readSVInt());
        firstSpanEvent.setServiceType(buffer.readShort());

        if (bitField.isSetRpc()) {
            firstSpanEvent.setRpc(buffer.readPrefixedString());
        }

        if (bitField.isSetEndPoint()) {
            firstSpanEvent.setEndPoint(buffer.readPrefixedString());
        }
        if (bitField.isSetDestinationId()) {
            firstSpanEvent.setDestinationId(buffer.readPrefixedString());
        }

        firstSpanEvent.setApiId(buffer.readSVInt());

        if (bitField.isSetNextSpanId()) {
            firstSpanEvent.setNextSpanId(buffer.readLong());
        }

        if (bitField.isSetHasException()) {
            int exceptionId = buffer.readSVInt();
            String exceptionMessage = buffer.readPrefixedString();
            firstSpanEvent.setExceptionInfo(exceptionId, exceptionMessage);
        }

        if (bitField.isSetAnnotation()) {
            List<AnnotationBo> annotationBoList = readAnnotationList(buffer, decodingContext);
            firstSpanEvent.setAnnotationBoList(annotationBoList);
        }

        if (bitField.isSetNextAsyncId()) {
            firstSpanEvent.setNextAsyncId(buffer.readSVInt());
        }

//        if (bitField.isSetAsyncId()) {
//            firstSpanEvent.setAsyncId(buffer.readInt());
//            firstSpanEvent.setAsyncSequence((short) buffer.readVInt());
//        }
        return firstSpanEvent;
    }

    private List<AnnotationBo> readAnnotationList(Buffer buffer, SpanDecodingContext decodingContext) {
        int annotationListSize = buffer.readVInt();
        List<AnnotationBo> annotationBoList = new ArrayList<>(annotationListSize);

//        AnnotationBo prev = decodingContext.getPrevFirstAnnotationBo();
        AnnotationBo prev = null;
        for (int i = 0; i < annotationListSize; i++) {
            AnnotationBo current;
            if (i == 0) {
                current = readFirstAnnotationBo(buffer);
                // save first annotation for delta bitfield
//                decodingContext.setPrevFirstAnnotationBo(current);
            } else {
                current = readDeltaAnnotationBo(buffer, prev);
            }

            prev = current;
            annotationBoList.add(current);
        }
        return annotationBoList;
    }

    private AnnotationBo readFirstAnnotationBo(Buffer buffer) {
        AnnotationBo current;
        current = new AnnotationBo();
        current.setKey(buffer.readSVInt());

        byte valueType = buffer.readByte();
        byte[] valueBytes = buffer.readPrefixedBytes();
        Object value = transcoder.decode(valueType, valueBytes);

        current.setValueType(valueType);
        current.setValue(value);
        return current;
    }

    private AnnotationBo readDeltaAnnotationBo(Buffer buffer, AnnotationBo prev) {
        AnnotationBo annotation = new AnnotationBo();

        final int prevKey = prev.getKey();

        annotation.setKey(buffer.readSVInt() + prevKey);

        byte valueType = buffer.readByte();
        byte[] valueBytes = buffer.readPrefixedBytes();
        Object value = transcoder.decode(valueType, valueBytes);
        annotation.setValueType(valueType);
        annotation.setValue(value);
        return annotation;
    }


    private SpanEventBo readQualifier(SpanAdaptor span, Buffer buffer) {
        String applicationId = buffer.readPrefixedString();
        span.setApplicationId(applicationId);

        String agentId = buffer.readPrefixedString();
        span.setAgentId(agentId);

        long agentStartTime = buffer.readVLong();
        span.setAgentStartTime(agentStartTime);

        long spanId = buffer.readLong();
        span.setSpanId(spanId);

        int firstSpanEventSequence = buffer.readSVInt();
        if (firstSpanEventSequence == -1) {
//            buffer.readByte();
            // spanEvent not exist ??
            logger.info("firstSpanEvent is null. bug!!!!");
            return null;
        } else {
            return readQualifierFirstSpanEvent(buffer);
        }
    }

    private SpanEventBo readQualifierFirstSpanEvent(Buffer buffer) {
        final SpanEventBo firstSpanEvent = new SpanEventBo();

        final byte bitField = buffer.readByte();
        if (SpanEventQualifierBitField.isSetAsync(bitField)) {
            int asyncId = buffer.readInt();
            int asyncSequence = buffer.readVInt();
            firstSpanEvent.setAsyncId(asyncId);
            firstSpanEvent.setAsyncSequence((short) asyncSequence);
        }
        return firstSpanEvent;
    }

    @Override
    public void next(SpanDecodingContext decodingContext) {
        decodingContext.next();
    }


    // resolve type miss match
    private interface SpanAdaptor {
        void setApplicationId(String applicationId);

        void setAgentId(String agentId);

        void setAgentStartTime(long agentStartTime);

        void setSpanId(long spanId);
    }

    private static class SpanBoAdaptor implements SpanAdaptor {
        private SpanBo spanBo;

        private SpanBoAdaptor(SpanBo spanBo) {
            if (spanBo == null) {
                throw new NullPointerException("spanBo must not be null");
            }
            this.spanBo = spanBo;
        }

        @Override
        public void setApplicationId(String applicationId) {
            this.spanBo.setApplicationId(applicationId);
        }

        @Override
        public void setAgentId(String agentId) {
            this.spanBo.setAgentId(agentId);
        }

        @Override
        public void setAgentStartTime(long agentStartTime) {
            this.spanBo.setAgentStartTime(agentStartTime);
        }

        @Override
        public void setSpanId(long spanId) {
            this.spanBo.setSpanId(spanId);
        }
    }

    private static class SpanChunkBoAdaptor implements SpanAdaptor {
        private SpanChunkBo spanChunkBo;

        private SpanChunkBoAdaptor(SpanChunkBo spanChunkBo) {
            if (spanChunkBo == null) {
                throw new NullPointerException("spanChunkBo must not be null");
            }
            this.spanChunkBo = spanChunkBo;
        }

        @Override
        public void setApplicationId(String applicationId) {
            this.spanChunkBo.setApplicationId(applicationId);
        }

        @Override
        public void setAgentId(String agentId) {
            this.spanChunkBo.setAgentId(agentId);
        }

        @Override
        public void setAgentStartTime(long agentStartTime) {
            this.spanChunkBo.setAgentStartTime(agentStartTime);
        }

        @Override
        public void setSpanId(long spanId) {
            this.spanChunkBo.setSpanId(spanId);
        }
    }
}
