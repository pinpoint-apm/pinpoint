/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.collector.handler.grpc;

import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.service.ExceptionMetaDataService;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import com.navercorp.pinpoint.common.server.bo.exception.StackTraceElementWrapperBo;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.MessageFormatUtils;
import com.navercorp.pinpoint.grpc.trace.PException;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PStackTraceElement;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.io.request.ServerHeader;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
@Service
public class GrpcExceptionMetaDataHandler implements RequestResponseHandler<PExceptionMetaData, PResult> {

    private static final String EMPTY = "";
    private final Logger logger = LogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ExceptionMetaDataService exceptionMetaDataService;

    public GrpcExceptionMetaDataHandler(ExceptionMetaDataService exceptionMetaDataService) {
        this.exceptionMetaDataService = Objects.requireNonNull(exceptionMetaDataService, "exceptionMetaDataService");
    }

    @Override
    public void handleRequest(ServerRequest<PExceptionMetaData> serverRequest, ServerResponse<PResult> serverResponse) {
        final PExceptionMetaData exceptionMetaData = serverRequest.getData();
        final ServerHeader header = serverRequest.getHeader();

        PResult result = handleExceptionMetaData(header, exceptionMetaData);
        serverResponse.write(result);

    }

    private PResult handleExceptionMetaData(final ServerHeader header, final PExceptionMetaData exceptionMetaData) {
        if (isDebug) {
            logger.debug("Handle PExceptionMetaData={}", MessageFormatUtils.debugLog(exceptionMetaData));
        }

        try {
            ExceptionMetaDataBo exceptionMetaDataBo = mapExceptionMetaDataBo(header, exceptionMetaData);

            List<ExceptionWrapperBo> exceptionWrapperBos = mapExceptionWrapperBo(
                    exceptionMetaData.getExceptionsList(), header
            );
            exceptionMetaDataBo.setExceptionWrapperBos(exceptionWrapperBos);


            exceptionMetaDataService.save(exceptionMetaDataBo);

            return PResults.SUCCESS;
        } catch (Exception e) {
            logger.warn("Failed to handle exceptionMetaData={}", MessageFormatUtils.debugLog(exceptionMetaData), e);
            // Avoid detailed error messages.
            return PResults.INTERNAL_SERVER_ERROR;
        }
    }

    private ExceptionMetaDataBo mapExceptionMetaDataBo(
            ServerHeader agentInfo, PExceptionMetaData exceptionMetaData
    ) {
        final String agentId = agentInfo.getAgentId();
        final TransactionId transactionId = newTransactionId(exceptionMetaData.getTransactionId(), agentId);

        return new ExceptionMetaDataBo(
                transactionId, exceptionMetaData.getSpanId(),
                (short) agentInfo.getServiceType(),
                agentInfo.getApplicationName(),
                agentInfo.getAgentId(),
                StringUtils.defaultIfEmpty(exceptionMetaData.getUriTemplate(), EMPTY)
        );
    }

    private List<ExceptionWrapperBo> mapExceptionWrapperBo(
            List<PException> exceptions, final ServerHeader header
    ) {
        List<ExceptionWrapperBo> list = new ArrayList<>(exceptions.size());
        for (PException p : exceptions) {
            ExceptionWrapperBo exceptionWrapperBo = new ExceptionWrapperBo(
                    StringUtils.defaultIfEmpty(p.getExceptionClassName(), EMPTY),
                    StringUtils.defaultIfEmpty(p.getExceptionMessage(), EMPTY),
                    getFallbackTime(p.getStartTime(), p, header),
                    p.getExceptionId(), p.getExceptionDepth(),
                    handleStackTraceElements(p.getStackTraceElementList())
            );
            list.add(exceptionWrapperBo);
        }
        return list;
    }

    private long getFallbackTime(long actual, PException p, final ServerHeader header) {
        if (actual > 0) {
            return actual;
        }
        logger.warn("Invalid StartTime. Fallback to current time. actual={} {} {} {} {} {}",
                actual, header.getApplicationName(), header.getAgentId(),
                p.getExceptionClassName(), p.getExceptionId(), p.getExceptionDepth()
        );
        return System.currentTimeMillis();
    }

    private List<StackTraceElementWrapperBo> handleStackTraceElements(List<PStackTraceElement> pStackTraceElements) {
        List<StackTraceElementWrapperBo> list = new ArrayList<>(pStackTraceElements.size());
        for (PStackTraceElement p : pStackTraceElements) {
            StackTraceElementWrapperBo stackTraceElementWrapperBo = new StackTraceElementWrapperBo(
                    StringUtils.defaultIfEmpty(p.getClassName(), EMPTY),
                    StringUtils.defaultIfEmpty(p.getFileName(), EMPTY),
                    p.getLineNumber(),
                    StringUtils.defaultIfEmpty(p.getMethodName(), EMPTY)
            );
            list.add(stackTraceElementWrapperBo);
        }
        return list;
    }

    private TransactionId newTransactionId(PTransactionId pTransactionId, String spanAgentId) {
        final String transactionAgentId = pTransactionId.getAgentId();
        if (StringUtils.hasLength(transactionAgentId)) {
            return TransactionId.of(transactionAgentId, pTransactionId.getAgentStartTime(), pTransactionId.getSequence());
        } else {
            return TransactionId.of(spanAgentId, pTransactionId.getAgentStartTime(), pTransactionId.getSequence());
        }
    }
}
