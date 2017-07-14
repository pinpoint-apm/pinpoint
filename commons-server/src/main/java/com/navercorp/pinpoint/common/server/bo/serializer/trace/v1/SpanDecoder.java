package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AnnotationBoDecoder annotationBoDecoder = new AnnotationBoDecoder();

    public SpanBo decodeSpanBo(Buffer qualifier, Buffer valueBuffer, SpanDecodingContext decodingContext) {
        TransactionId transactionId = decodingContext.getTransactionId();

        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(transactionId);

        long spanId = qualifier.readLong();
        spanBo.setSpanId(spanId);

        spanBo.setCollectorAcceptTime(decodingContext.getCollectorAcceptedTime());

        readSpan(spanBo, valueBuffer);
        if (logger.isDebugEnabled()) {
            logger.debug("read span :{}", spanBo);
        }
        return spanBo;
    }


    // for test
    int readSpan(SpanBo span, Buffer buffer) {

        span.setVersion(buffer.readByte());

        span.setAgentId(buffer.readPrefixedString());
        span.setAgentStartTime(buffer.readVLong());

        // this.spanID = buffer.readLong();
        span.setParentSpanId(buffer.readLong());

        span.setStartTime(buffer.readVLong());
        span.setElapsed(buffer.readVInt());

        span.setRpc(buffer.readPrefixedString());
        span.setApplicationId(buffer.readPrefixedString());
        span.setServiceType(buffer.readShort());
        span.setEndPoint(buffer.readPrefixedString());
        span.setRemoteAddr(buffer.readPrefixedString());
        span.setApiId(buffer.readSVInt());

        span.setErrCode(buffer.readSVInt());

        final boolean hasException = buffer.readBoolean();
        if (hasException) {
            int exceptionId = buffer.readSVInt();
            String exceptionMessage = buffer.readPrefixedString();
            span.setExceptionInfo(exceptionId, exceptionMessage);

        }

        span.setFlag(buffer.readShort());

        // FIXME (2015.03) Legacy - applicationServiceType added in v1.1.0
        // Defaults to span's service type for older versions where applicationServiceType does not exist.
        if (buffer.hasRemaining()) {
            final boolean hasApplicationServiceType = buffer.readBoolean();
            if (hasApplicationServiceType) {
                span.setApplicationServiceType(buffer.readShort());
            }
        }

        if (buffer.hasRemaining()) {
            span.setLoggingTransactionInfo(buffer.readByte());
        }

        if (buffer.hasRemaining()) {
            span.setAcceptorHost(buffer.readPrefixedString());
        }

        return buffer.getOffset();
    }

    public SpanEventBo decodeSpanEventBo(Buffer qualifier, Buffer valueBuffer, SpanDecodingContext decodingContext) {
        SpanEventBo spanEventBo = new SpanEventBo();

        long spanId = qualifier.readLong();
        decodingContext.setSpanId(spanId);

        short sequence = qualifier.readShort();
        int asyncId = -1;
        if (qualifier.hasRemaining()) {
            asyncId = qualifier.readInt();
        }
        short asyncSequence = -1;
        if (qualifier.hasRemaining()) {
            asyncSequence = qualifier.readShort();
        }
        spanEventBo.setSequence(sequence);
        spanEventBo.setAsyncId(asyncId);
        spanEventBo.setAsyncSequence(asyncSequence);

        readSpanEvent(spanEventBo, valueBuffer, decodingContext);
        if (logger.isDebugEnabled()) {
            logger.debug("read spanEvent :{}", spanEventBo);
        }
        return spanEventBo;
    }

    // for test
    int readSpanEvent(final SpanEventBo spanEvent, Buffer buffer, SpanDecodingContext decodingContext) {

        spanEvent.setVersion(buffer.readByte());

        decodingContext.setAgentId(buffer.readPrefixedString());
        decodingContext.setApplicationId(buffer.readPrefixedString());
        decodingContext.setAgentStartTime(buffer.readVLong());

        spanEvent.setStartElapsed(buffer.readVInt());
        spanEvent.setEndElapsed(buffer.readVInt());

        // don't need to get sequence because it can be got at Qualifier
        // this.sequence = buffer.readShort();


        spanEvent.setRpc(buffer.readPrefixedString());
        spanEvent.setServiceType(buffer.readShort());
        spanEvent.setEndPoint(buffer.readPrefixedString());
        spanEvent.setDestinationId(buffer.readPrefixedString());
        spanEvent.setApiId(buffer.readSVInt());

        spanEvent.setDepth(buffer.readSVInt());
        spanEvent.setNextSpanId(buffer.readLong());

        final boolean hasException = buffer.readBoolean();
        if (hasException) {
            spanEvent.setExceptionInfo(buffer.readSVInt(), buffer.readPrefixedString());
        }

        final List<AnnotationBo> annotationBoList = annotationBoDecoder.decode(buffer);
        spanEvent.setAnnotationBoList(annotationBoList);
        if (buffer.hasRemaining()) {
            spanEvent.setNextAsyncId(buffer.readSVInt());
        }

        return buffer.getOffset();
    }

    public void next(SpanDecodingContext decodingContext) {
        decodingContext.next();
    }
}
