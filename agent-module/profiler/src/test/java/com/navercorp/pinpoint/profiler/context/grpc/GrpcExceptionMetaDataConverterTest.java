package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PException;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PStackTraceElement;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaData;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaDataFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ExceptionMetaDataMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ExceptionMetaDataMapperImpl;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author intr3p1d
 */
class GrpcExceptionMetaDataConverterTest {


    ExceptionMetaDataFactory exceptionMetaDataFactory = new ExceptionMetaDataFactory(newTraceRoot());
    ExceptionWrapperFactory wrapperFactory = new ExceptionWrapperFactory(0, 3000);


    ExceptionMetaDataMapper exceptionMetaDataMapper = new ExceptionMetaDataMapperImpl();
    GrpcExceptionMetaDataConverter grpcExceptionMetaDataConverter = new GrpcExceptionMetaDataConverter(exceptionMetaDataMapper);

    private static TraceRoot newTraceRoot() {
        TraceRoot traceRoot = mock(TraceRoot.class);
        final String agentId = "agent";
        final long agentStartTime = System.currentTimeMillis();
        when(traceRoot.getTraceId()).thenReturn(new DefaultTraceId(agentId, agentStartTime, 0));
        when(traceRoot.getTraceStartTime()).thenReturn(agentStartTime + 100);
        when(traceRoot.getLocalTransactionId()).thenReturn((long) 1);

        Shared shared = mock(Shared.class);
        when(shared.getUriTemplate()).thenReturn("/api/test");
        when(traceRoot.getShared()).thenReturn(shared);

        return traceRoot;
    }

    @Test
    void testMapExceptionMetaData() {
        List<ExceptionWrapper> wrappers = new ArrayList<>();
        wrapperFactory.addAllExceptionWrappers(wrappers, new RuntimeException(), null, 1, 1, 1);
        ExceptionMetaData exceptionMetaData = exceptionMetaDataFactory.newExceptionMetaData(
                wrappers
        );
        PExceptionMetaData pExceptionMetaData = (PExceptionMetaData) grpcExceptionMetaDataConverter.toMessage(exceptionMetaData);

        assertEquals(exceptionMetaData.getExceptionWrappers().size(), pExceptionMetaData.getExceptionsList().size());
        for (int i = 0; i < exceptionMetaData.getExceptionWrappers().size(); i++) {
            ExceptionWrapper exceptionWrapper = exceptionMetaData.getExceptionWrappers().get(i);
            PException pException = pExceptionMetaData.getExceptions(i);

            assertEquals(exceptionWrapper.getExceptionClassName(), pException.getExceptionClassName());
            assertEquals(exceptionWrapper.getExceptionMessage(), pException.getExceptionMessage());
            assertEquals(exceptionWrapper.getStartTime(), pException.getStartTime());
            assertEquals(exceptionWrapper.getExceptionId(), pException.getExceptionId());
            assertEquals(exceptionWrapper.getExceptionDepth(), pException.getExceptionDepth());


            StackTraceElement[] stackTraceElements = exceptionWrapper.getStackTraceElements();
            List<PStackTraceElement> pStackTraceElements = pException.getStackTraceElementList();
            assertEquals(stackTraceElements.length, pStackTraceElements.size());
            for (int j = 0; j < stackTraceElements.length; j++) {
                StackTraceElement stackTraceElement = stackTraceElements[j];
                PStackTraceElement pStackTraceElement = pStackTraceElements.get(j);
                assertEquals(stackTraceElement.getClassName(), pStackTraceElement.getClassName());
                assertEquals(stackTraceElement.getFileName(), pStackTraceElement.getFileName());
                assertEquals(stackTraceElement.getLineNumber(), pStackTraceElement.getLineNumber());
                assertEquals(stackTraceElement.getMethodName(), pStackTraceElement.getMethodName());
            }


        }
    }
}