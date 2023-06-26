package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.grpc.trace.PException;
import com.navercorp.pinpoint.grpc.trace.PSpanEventException;
import com.navercorp.pinpoint.grpc.trace.PStackTraceElement;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.context.exception.model.SpanEventException;

import java.util.ArrayList;
import java.util.List;

public class GrpcExceptionTraceConverter {

    private final PSpanEventException.Builder pSpanEventExceptionBuilder = PSpanEventException.newBuilder();
    private final PException.Builder pExceptionBuilder = PException.newBuilder();

    public PSpanEventException buildPSpanEventException(SpanEventException spanEventException) {
        final PSpanEventException.Builder pSpanEventException = getSpanEventExceptionBuilder();
        List<ExceptionWrapper> exceptionWrappers = spanEventException.getExceptionWrappers();
        if (exceptionWrappers != null) {
            pSpanEventException.addAllExceptions(buildPExceptions(exceptionWrappers));
        }
        pSpanEventException.setStartTime(spanEventException.getStartTime());
        pSpanEventException.setExceptionId(spanEventException.getExceptionId());
        return pSpanEventException.build();
    }

    List<PException> buildPExceptions(List<ExceptionWrapper> exceptionWrappers) {
        final List<PException> tExceptionList = new ArrayList<>(exceptionWrappers.size());
        for (ExceptionWrapper exceptionWrapper : exceptionWrappers) {
            final PException pException = buildException(exceptionWrapper).build();
            tExceptionList.add(pException);
        }
        return tExceptionList;
    }

    private PException.Builder buildException(ExceptionWrapper exceptionWrapper) {
        final PException.Builder pException = getExceptionBuilder();
        pException.setExceptionClassName(exceptionWrapper.getExceptionClassName());
        pException.setExceptionMessage(exceptionWrapper.getExceptionMessage());

        final StackTraceElement[] stackTraceElements = exceptionWrapper.getStackTraceElements();
        if (ArrayUtils.hasLength(stackTraceElements)) {
            final List<PStackTraceElement> pStackTraceElements = new ArrayList<>();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                pStackTraceElements.add(buildStackTraceElement(stackTraceElement));
            }
            pException.addAllStackTraceElement(pStackTraceElements);
        }

        return pException;
    }

    private PStackTraceElement buildStackTraceElement(StackTraceElement stackTraceElement) {
        final PStackTraceElement.Builder builder = PStackTraceElement.newBuilder();
        builder.setClassName(stackTraceElement.getClassName());
        builder.setFileName(stackTraceElement.getFileName());
        builder.setLineNumber(stackTraceElement.getLineNumber());
        builder.setMethodName(stackTraceElement.getMethodName());
        return builder.build();
    }


    private PSpanEventException.Builder getSpanEventExceptionBuilder() {
        pSpanEventExceptionBuilder.clear();
        return pSpanEventExceptionBuilder;
    }

    private PException.Builder getExceptionBuilder() {
        pExceptionBuilder.clear();
        return pExceptionBuilder;
    }
}
