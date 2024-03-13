
package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.sampler.Sampler;
import com.navercorp.pinpoint.collector.sampler.SpanSamplerFactory;
import com.navercorp.pinpoint.collector.service.ApplicationInfoService;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.hbase.RequestNotPermittedException;
import com.navercorp.pinpoint.common.profiler.logging.LogSampler;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.grpc.BindAttribute;
import com.navercorp.pinpoint.common.server.bo.grpc.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
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
    private final LogSampler infoLog = new LogSampler(1000);
    private final LogSampler warnLog = new LogSampler(100);
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceService[] traceServices;

    private final GrpcSpanFactory spanFactory;

    private final AcceptedTimeService acceptedTimeService;
    private final ApplicationInfoService applicationInfoService;

    private final Sampler<BasicSpan> sampler;

    public GrpcSpanChunkHandler(
            TraceService[] traceServices,
            GrpcSpanFactory spanFactory,
            AcceptedTimeService acceptedTimeService,
            SpanSamplerFactory spanSamplerFactory,
            ApplicationInfoService applicationInfoService) {
        this.traceServices = Objects.requireNonNull(traceServices, "traceServices");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
        this.acceptedTimeService = Objects.requireNonNull(acceptedTimeService, "acceptedTimeService");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
        this.sampler = spanSamplerFactory.createBasicSpanSampler();

        logger.info("TraceServices {}", Arrays.toString(traceServices));
    }

    @Override
    public void handleSimple(ServerRequest<GeneratedMessageV3> serverRequest) {
        final GeneratedMessageV3 data = serverRequest.getData();
        if (data instanceof PSpanChunk spanChunk) {
            handleSpanChunk(spanChunk);
        } else {
            logger.warn("Invalid request type. serverRequest={}", serverRequest);
            throw Status.INTERNAL.withDescription("Bad Request(invalid request type)").asRuntimeException();
        }
    }


    private void handleSpanChunk(PSpanChunk spanChunk) {
        if (isDebug) {
            logger.debug("Handle PSpanChunk={}", createSimpleSpanChunkLog(spanChunk));
        }


        final Header header = ServerContext.getAgentInfo();
        final ApplicationId applicationId = this.applicationInfoService.getApplicationId(header.getApplicationName());
        final BindAttribute attribute = BindAttribute.of(header, applicationId, acceptedTimeService.getAcceptedTime());
        final SpanChunkBo spanChunkBo = spanFactory.buildSpanChunkBo(spanChunk, attribute);
        if (!sampler.isSampling(spanChunkBo)) {
            if (isDebug) {
                logger.debug("unsampled PSpanChunk={}", createSimpleSpanChunkLog(spanChunk));
            } else {
                infoLog.log(() -> {
                    if (logger.isInfoEnabled()) {
                        logger.info("unsampled PSpanChunk={}", createSimpleSpanChunkLog(spanChunk));
                    }
                });
            }
            return;
        }
        for (TraceService traceService : traceServices) {
            try {
                traceService.insertSpanChunk(spanChunkBo);
            } catch (RequestNotPermittedException notPermitted) {
                warnLog.log(c -> logger.warn("Failed to handle SpanChunk RequestNotPermitted:{} {}", notPermitted.getMessage(), c));
            } catch (Throwable e) {
                logger.warn("Failed to handle SpanChunk={}", MessageFormatUtils.debugLog(spanChunk), e);
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