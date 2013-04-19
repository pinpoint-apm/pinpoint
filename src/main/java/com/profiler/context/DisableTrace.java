package com.profiler.context;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.common.util.ParsingResult;
import com.profiler.interceptor.MethodDescriptor;

import java.util.List;

/**
 *
 */
public class DisableTrace  implements Trace {

    public static final DisableTrace INSTANCE = new DisableTrace();
    // 구지 객체를 생성하여 사용할 필요가 없을듯.
    private DisableTrace() {
    }

    @Override
    public AsyncTrace createAsyncTrace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockBegin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markBeforeTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getBeforeTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markAfterTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAfterTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockBegin(int stackId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceRootBlockEnd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockEnd() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TraceID getTraceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSampling() {
        // sampling false를 항상 false를 리턴한다.
        return false;
    }

    @Override
    public void recordException(Object result) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(int apiId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(int apiId, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordRpcName(String rpc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordDestinationId(String destinationId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordDestinationAddress(List<String> address) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordDestinationAddressList(List<String> addressList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordEndPoint(String endPoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordRemoteAddr(String remoteAddr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordNextSpanId(int spanId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAcceptorHost(String host) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStackFrameId() {
        throw new UnsupportedOperationException();
    }
}
