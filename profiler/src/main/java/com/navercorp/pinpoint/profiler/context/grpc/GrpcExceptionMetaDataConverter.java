package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.trace.PException;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PStackTraceElement;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaData;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.ArrayList;
import java.util.List;

public class GrpcExceptionMetaDataConverter implements MessageConverter<MetaDataType, GeneratedMessageV3> {

    private static final String EMPTY_STRING = "";

    private final PExceptionMetaData.Builder pExceptionMetaDataBuilder = PExceptionMetaData.newBuilder();
    private final PException.Builder pExceptionBuilder = PException.newBuilder();
    private final PStackTraceElement.Builder pStackTraceElementBuilder = PStackTraceElement.newBuilder();

    @Override
    public GeneratedMessageV3 toMessage(MetaDataType message) {
        if (message instanceof ExceptionMetaData) {
            final ExceptionMetaData exceptionMetaData = (ExceptionMetaData) message;
            return convertPExceptionMetaData(exceptionMetaData);
        }
        return null;
    }

    public PExceptionMetaData convertPExceptionMetaData(ExceptionMetaData exceptionMetaData) {
        final PExceptionMetaData.Builder builder = this.pExceptionMetaDataBuilder;
        try {
            List<ExceptionWrapper> exceptionWrappers = exceptionMetaData.getExceptionWrappers();
            if (exceptionWrappers != null) {
                builder.addAllExceptions(convertPExceptions(exceptionWrappers));
            }
            TraceRoot traceRoot = exceptionMetaData.getTraceRoot();
            final TraceId traceId = traceRoot.getTraceId();
            final PTransactionId transactionId = newTransactionId(traceId);
            builder.setTransactionId(transactionId);
            builder.setSpanId(traceId.getSpanId());
            builder.setUriTemplate(traceRoot.getShared().getUriTemplate());
            return builder.build();
        } finally {
            builder.clear();
        }
    }

    private PTransactionId newTransactionId(TraceId traceId) {
        final PTransactionId.Builder builder = PTransactionId.newBuilder();
        builder.setAgentId(traceId.getAgentId());
        builder.setAgentStartTime(traceId.getAgentStartTime());
        builder.setSequence(traceId.getTransactionSequence());
        return builder.build();
    }

    private List<PException> convertPExceptions(List<ExceptionWrapper> exceptionWrappers) {
        final List<PException> pExceptions = new ArrayList<>(exceptionWrappers.size());
        for (ExceptionWrapper exceptionWrapper : exceptionWrappers) {
            final PException pException = convertException(exceptionWrapper);
            pExceptions.add(pException);
        }
        return pExceptions;
    }

    private PException convertException(ExceptionWrapper exceptionWrapper) {
        final PException.Builder builder = this.pExceptionBuilder;
        try {
            builder.setExceptionClassName(exceptionWrapper.getExceptionClassName());
            builder.setExceptionMessage(exceptionWrapper.getExceptionMessage());

            builder.setStartTime(exceptionWrapper.getStartTime());
            builder.setExceptionId(exceptionWrapper.getExceptionId());
            builder.setExceptionDepth(exceptionWrapper.getExceptionDepth());

            final StackTraceElement[] stackTraceElements = exceptionWrapper.getStackTraceElements();
            if (ArrayUtils.hasLength(stackTraceElements)) {
                final List<PStackTraceElement> pStackTraceElements = new ArrayList<>();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    pStackTraceElements.add(convertStackTraceElement(stackTraceElement));
                }
                builder.addAllStackTraceElement(pStackTraceElements);
            }

            return builder.build();
        } finally {
            builder.clear();
        }
    }

    private PStackTraceElement convertStackTraceElement(StackTraceElement stackTraceElement) {
        final PStackTraceElement.Builder builder = this.pStackTraceElementBuilder;
        try {
            builder.setClassName(StringUtils.defaultIfEmpty(stackTraceElement.getClassName(), EMPTY_STRING));
            builder.setFileName(StringUtils.defaultIfEmpty(stackTraceElement.getFileName(), EMPTY_STRING));
            builder.setLineNumber(stackTraceElement.getLineNumber());
            builder.setMethodName(StringUtils.defaultIfEmpty(stackTraceElement.getMethodName(), EMPTY_STRING));
            return builder.build();
        } finally {
            builder.clear();
        }
    }
}
