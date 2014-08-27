package com.nhn.pinpoint.bootstrap.context;

import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 */
public interface RecordableTrace {

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    TraceId getTraceId();

    boolean canSampled();

    boolean isRoot();


    void recordException(Throwable throwable);

    void recordApi(MethodDescriptor methodDescriptor);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args);

    void recordApi(MethodDescriptor methodDescriptor, Object args, int index);

    void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end);

    void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index);

    ParsingResult recordSqlInfo(String sql);

    void recordSqlParsingResult(ParsingResult parsingResult);

    void recordSqlParsingResult(ParsingResult parsingResult, String bindValue);

    void recordAttribute(AnnotationKey key, String value);

    void recordAttribute(AnnotationKey key, int value);

    void recordAttribute(AnnotationKey key, Object value);

    void recordServiceType(ServiceType serviceType);

    void recordRpcName(String rpc);

    void recordDestinationId(String destinationId);

    void recordEndPoint(String endPoint);

    void recordRemoteAddress(String remoteAddress);

    void recordNextSpanId(long spanId);

    void recordParentApplication(String parentApplicationName, short parentApplicationType);

    /**
     * WAS_A -> WAS_B 호출 관계일 때 WAS_B에서 WAS_A가 보내준 호출 정보를 통해 자기 자신의 정보를 추출하여 저장
     * 이 데이터는 서버맵에서 WAS끼리 호출관계를 알아낼 떄 필요하다.
     * 
     * @param host host 값은 WAS를 호출한 URL상의 host를 가져와야 한다.
     */
    void recordAcceptorHost(String host);

    int getStackFrameId();
}
