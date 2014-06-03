package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.Metric;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.profiler.monitor.metric.MetricRegistry;
import com.nhn.pinpoint.profiler.monitor.metric.RpcMetric;

/**
 * @author emeroad
 */
public class RpcTrace implements Trace {

    private final Trace trace;

    private MetricRegistry metricRegistry;

    private RpcMetric rpcMetric;
    private String destinationId;

    public RpcTrace(Trace trace, MetricRegistry metricRegistry) {
        this.trace = trace;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void traceBlockBegin() {
        trace.traceBlockBegin();
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
    public void traceBlockBegin(int stackId) {
        trace.traceBlockBegin(stackId);
    }

    @Override
    public void traceRootBlockEnd() {
        trace.traceRootBlockEnd();
    }


    @Override
    public void traceRootBlockEnd(Metric responseMetric) {
        trace.traceRootBlockEnd(responseMetric);
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
    public TraceId getTraceId() {
        return trace.getTraceId();
    }

    @Override
    public boolean canSampled() {
        return trace.canSampled();
    }

    @Override
    public void recordException(Object result) {
        trace.recordException(result);
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
        this.rpcMetric = this.metricRegistry.getRpcMetric(serviceType);
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
}
