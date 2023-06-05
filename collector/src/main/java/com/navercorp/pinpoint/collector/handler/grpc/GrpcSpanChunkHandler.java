
package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanEvent;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.request.ServerRequest;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class GrpcSpanChunkHandler implements SimpleHandler<GeneratedMessageV3> {

    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceService[] traceServices;

    private final GrpcSpanFactory spanFactory;

    public GrpcSpanChunkHandler(TraceService[] traceServices, GrpcSpanFactory spanFactory) {
        this.traceServices = Objects.requireNonNull(traceServices, "traceServices");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");

        logger.info("TraceServices {}", Arrays.toString(traceServices));
    }

    @Override
    public void handleSimple(ServerRequest<GeneratedMessageV3> serverRequest) {
        final GeneratedMessageV3 data = serverRequest.getData();
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


        final Header agentInfo = ServerContext.getAgentInfo();
        final SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(spanChunk, agentInfo);
        for (TraceService traceService : traceServices) {
            try {
                traceService.insertSpanChunk(spanChunkBo);
            } catch (Exception e) {
                logger.warn("Failed to handle spanChunk={}", MessageFormatUtils.debugLog(spanChunk), e);
            }
        }
    }

    private String createSimpleSpanChunkLog(PSpanChunk spanChunk) {
        if (!isDebug) {
            return "";
        }

        StringBuilder log = new StringBuilder(64);

        PTransactionId transactionId = spanChunk.getTransactionId();
        log.append(" transactionId:");
        log.append(MessageFormatUtils.debugLog(transactionId));

        log.append(" spanId:").append(spanChunk.getSpanId());

        final List<PSpanEvent> spanEventList = spanChunk.getSpanEventList();
        if (CollectionUtils.hasLength(spanEventList)) {
            log.append(" spanEventSequence:");
            for (PSpanEvent pSpanEvent : spanEventList) {
                if (pSpanEvent == null) {
                    continue;
                }
                log.append(pSpanEvent.getSequence()).append(" ");
            }
        }

        return log.toString();
    }

}