
package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class GrpcSpanChunkHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceService traceService;

    private final GrpcSpanFactory spanFactory;

    @Autowired
    public GrpcSpanChunkHandler(TraceService traceService, GrpcSpanFactory spanFactory) {
        this.traceService = Objects.requireNonNull(traceService, "traceService");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (data instanceof PSpanChunk) {
            handleSpanChunk((PSpanChunk) data);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }


    private void handleSpanChunk(PSpanChunk spanChunk) {
        if (isDebug) {
            logger.debug("Handle PSpanChunk={}", createSimpleSpanChunkLog(spanChunk));
        }

        try {
            final Header agentInfo = ServerContext.getAgentInfo();
            final SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(spanChunk, agentInfo);
            this.traceService.insertSpanChunk(spanChunkBo);
        } catch (Exception e) {
            logger.warn("Failed to handle spanChunk={}", MessageFormatUtils.debugLog(spanChunk), e);
        }
    }

    private String createSimpleSpanChunkLog(PSpanChunk spanChunk) {
        if (!isDebug) {
            return "";
        }

        StringBuilder log = new StringBuilder();

        PTransactionId transactionId = spanChunk.getTransactionId();
        log.append(" transactionId:");
        log.append(MessageFormatUtils.debugLog(transactionId));

        log.append(" spanId:").append(spanChunk.getSpanId());


        StringBuilder spanEventSequenceLog = new StringBuilder();
        List<PSpanEvent> spanEventList = spanChunk.getSpanEventList();
        for (PSpanEvent pSpanEvent : spanEventList) {
            if (pSpanEvent == null) {
                continue;
            }
            spanEventSequenceLog.append(pSpanEvent.getSequence()).append(" ");
        }

        log.append(" spanEventSequence:").append(spanEventSequenceLog.toString());

        return log.toString();
    }

}