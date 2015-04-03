package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.ParsingResult;

public class AsyncTrace implements Trace {

    private final Trace trace;
    private int asyncId;

    public AsyncTrace(final Trace trace) {
        this.trace = trace;
    }

    public void setAsyncId(final int asyncId) {
        this.asyncId = asyncId;
    }

    public int getAsyncId() {
        return asyncId;
    }

    @Override
    public void markBeforeTime() {
        trace.markBeforeTime();
    }

    @Override
    public long getBeforeTime() {
        return trace.getBeforeTime();
    }

    @Override
    public void markAfterTime() {
        trace.markAfterTime();
    }

    @Override
    public long getAfterTime() {
        return trace.getAfterTime();
    }

    @Override
    public TraceId getTraceId() {
        return trace.getTraceId();
    }

    @Override
    public boolean canSampled() {
        return trace.canSampled();
    }

    @Override
    public boolean isRoot() {
        return trace.isRoot();
    }

    @Override
    public short getServiceType() {
        return trace.getServiceType();
    }

    @Override
    public void recordException(Throwable throwable) {
        trace.recordException(throwable);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
        trace.recordApi(methodDescriptor);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        trace.recordApi(methodDescriptor, args);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
        trace.recordApi(methodDescriptor, args, index);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
        trace.recordApi(methodDescriptor, args, start, end);
    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
        trace.recordApiCachedString(methodDescriptor, args, index);
    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        return trace.recordSqlInfo(sql);
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
        trace.recordSqlParsingResult(parsingResult);
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {
        trace.recordSqlParsingResult(parsingResult, bindValue);
    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {
        trace.recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {
        trace.recordAttribute(key, value);
    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {
        trace.recordAttribute(key, value);
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        trace.recordServiceType(serviceType);
    }

    @Override
    public void recordRpcName(String rpc) {
        trace.recordRpcName(rpc);
    }

    @Override
    public void recordDestinationId(String destinationId) {
        trace.recordDestinationId(destinationId);
    }

    @Override
    public void recordEndPoint(String endPoint) {
        trace.recordEndPoint(endPoint);
    }

    @Override
    public void recordRemoteAddress(String remoteAddress) {
        trace.recordRemoteAddress(remoteAddress);
    }

    @Override
    public void recordNextSpanId(long spanId) {
        trace.recordNextSpanId(spanId);
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {
        trace.recordParentApplication(parentApplicationName, parentApplicationType);
    }

    @Override
    public void recordAcceptorHost(String host) {
        trace.recordAcceptorHost(host);
    }

    @Override
    public int getStackFrameId() {
        return trace.getStackFrameId();
    }

    @Override
    public Object getAttribute(String key) {
        return trace.getAttribute(key);
    }

    @Override
    public Object setAttribute(String key, Object value) {
        return trace.setAttribute(key, value);
    }

    @Override
    public Object removeAttribute(String key) {
        return trace.removeAttribute(key);
    }

    @Override
    public void recordAsyncId(int asyncId) {
        trace.recordAsyncId(asyncId);
    }

    @Override
    public void recordnextAsyncId(int asyncId) {
        trace.recordnextAsyncId(asyncId);
    }

    @Override
    public void traceBlockBegin() {
        trace.traceBlockBegin();
        trace.recordAsyncId(asyncId);
    }

    @Override
    public void traceBlockBegin(int stackId) {
        trace.traceBlockBegin(stackId);
    }

    @Override
    public void traceRootBlockEnd() {
        trace.traceRootBlockEnd();
    }

    @Override
    public void traceBlockEnd() {
        trace.traceBlockEnd();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        trace.traceBlockEnd(stackId);
    }

    @Override
    public Object setTraceBlockAttachment(Object attachment) {
        return trace.setTraceBlockAttachment(attachment);
    }

    @Override
    public Object getTraceBlockAttachment() {
        return trace.getTraceBlockAttachment();
    }

    @Override
    public Object removeTraceBlockAttachment() {
        return trace.removeTraceBlockAttachment();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public long getTraceStartTime() {
        return trace.getTraceStartTime();
    }

    @Override
    public boolean isRootStack() {
        return trace.isRootStack();
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        return trace.getAsyncTraceId();
    }
}
