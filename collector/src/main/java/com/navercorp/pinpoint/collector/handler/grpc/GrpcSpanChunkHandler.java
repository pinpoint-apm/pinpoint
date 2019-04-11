
package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.io.request.ServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class GrpcSpanChunkHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TraceService traceService;

    private final GrpcSpanFactory spanFactory;

    @Autowired
    public GrpcSpanChunkHandler(TraceService traceService, GrpcSpanFactory spanFactory) {
        this.traceService = Objects.requireNonNull(traceService, "traceService must not be null");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory must not be null");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle simple data={}", data);
        }
        if (data instanceof PSpanChunk) {
            handleSpanChunk((PSpanChunk) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }


    private void handleSpanChunk(PSpanChunk pSpanChunk) {
        if (logger.isDebugEnabled()) {
            logger.debug("Handle PSpanChunk={}", pSpanChunk);
        }
        AgentHeaderFactory.Header agentInfo = ServerContext.getAgentInfo();
        SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(pSpanChunk, agentInfo);

        try {
            this.traceService.insertSpanChunk(spanChunkBo);
        } catch (Exception e) {
            logger.warn("SpanChunk handle error Caused:{}", e.getMessage(), e);
        }
    }
}