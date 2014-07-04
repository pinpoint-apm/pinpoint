package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;

/**
 * @author emeroad
 */
public class MockTrace implements Trace {

    private long beforeTime;
    private long afterTime;

    private boolean sampled = true;

    @Override
    public void markBeforeTime() {
        beforeTime = System.currentTimeMillis();
    }

    @Override
    public long getBeforeTime() {
        return beforeTime;
    }

    @Override
    public void markAfterTime() {
        afterTime = System.currentTimeMillis();
    }

    @Override
    public long getAfterTime() {
        return afterTime;
    }

    @Override
    public TraceId getTraceId() {
        return null;
    }

    public void setSampled(boolean sampled) {
        this.sampled = sampled;
    }

    @Override
    public boolean canSampled() {
        return sampled;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public void recordException(Throwable throwable) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {

    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {

    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {

    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        return null;
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {

    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {

    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {

    }

    @Override
    public void recordServiceType(ServiceType serviceType) {

    }

    @Override
    public void recordRpcName(String rpc) {

    }

    @Override
    public void recordDestinationId(String destinationId) {

    }

    @Override
    public void recordEndPoint(String endPoint) {

    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {

    }

    @Override
    public void recordNextSpanId(long spanId) {

    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {

    }

    @Override
    public void recordAcceptorHost(String host) {

    }

    @Override
    public int getStackFrameId() {
        return 0;
    }

    @Override
    public void traceBlockBegin() {

    }

    @Override
    public void traceBlockBegin(int stackId) {

    }

    @Override
    public void traceRootBlockEnd() {

    }

    @Override
    public void traceBlockEnd() {

    }

    @Override
    public void traceBlockEnd(int stackId) {

    }
}
