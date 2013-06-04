package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.interceptor.MethodDescriptor;

import java.util.List;

/**
 *
 */
public interface Trace {

    public static final int NOCHECK_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    AsyncTrace createAsyncTrace();

    void traceBlockBegin();

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    void traceBlockBegin(int stackId);

    void traceRootBlockEnd();

    void traceBlockEnd();

    void traceBlockEnd(int stackId);

    TraceID getTraceId();

    boolean canSampled();


    void recordException(Object result);

    void recordApi(MethodDescriptor methodDescriptor);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args);

    void recordApi(int apiId);

    void recordApi(int apiId, Object[] args);

    void recordAttribute(AnnotationKey key, String value);

    ParsingResult recordSqlInfo(String sql);

    void recordSqlParsingResult(ParsingResult parsingResult);

    void recordAttribute(AnnotationKey key, Object value);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpc);

    void recordDestinationId(String destinationId);

    void recordDestinationAddress(List<String> address);

    void recordDestinationAddressList(List<String> addressList);

    void recordEndPoint(String endPoint);

    void recordRemoteAddr(String remoteAddr);

    void recordNextSpanId(int spanId);

    void setTraceContext(TraceContext traceContext);

    void recordParentApplication(String parentApplicationName, short parentApplicationType);

    void recordAcceptorHost(String host);

    int getStackFrameId();
}
