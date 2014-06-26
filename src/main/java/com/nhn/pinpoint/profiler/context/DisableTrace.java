package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.Metric;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;


/**
 * @author emeroad
 */
public class DisableTrace  implements Trace {

    public static final DisableTrace INSTANCE = new DisableTrace();
    // 구지 객체를 생성하여 사용할 필요가 없을듯.
    private DisableTrace() {
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
    public TraceId getTraceId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSampled() {
        // sampling false를 항상 false를 리턴한다.
        return false;
    }

    @Override
    public boolean isRoot() {
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
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
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

    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {
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
    public void recordEndPoint(String endPoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void recordNextSpanId(long spanId) {
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
