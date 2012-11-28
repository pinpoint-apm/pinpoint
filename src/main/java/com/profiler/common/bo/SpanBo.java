package com.profiler.common.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.util.Buffer;
import com.profiler.common.util.BytesUtils;

/**
 *
 */
public class SpanBo {

    private static final int VERSION_SIZE = 1;
    // version 0 = prefix의 사이즈를 int로
    // version 1 = prefix의 사이즈를 short로
    // version 2 = prefix의 사이즈를 byte 하면 byte eocnding이 좀 줄지 않나?
    private byte version = 0;

    // private static final int MOSTTRACEID = 8;
    // private static final int LEASTTRACEID = 8;
    // private static final int SPANID = 8;
    private static final int PARENTSPANID = 8;
    // private static final int TIMESTAMP = 8;
    private static final int SERVICETYPE = 2;
    private static final int FLAG = 2;

    private String agentId;
    private long mostTraceId;
    private long leastTraceId;
    private long spanId;
    private long parentSpanId;
    private long startTime;
    private int elapsed;
    private String rpc;
    private String serviceName;
    private ServiceType serviceType;
    private String endPoint;
    private List<AnnotationBo> annotationBoList;
    private short flag; // optional

    private int recursiveCallCount = 0;

    public SpanBo(Span span) {
        this.agentId = span.getAgentId();

        this.mostTraceId = span.getMostTraceId();
        this.leastTraceId = span.getLeastTraceId();

        this.spanId = span.getSpanId();
        this.parentSpanId = span.getParentSpanId();

        this.startTime = span.getStartTime();
        this.elapsed = span.getElapsed();

        this.rpc = span.getRpc();
        this.serviceName = span.getServiceName();
        this.serviceType = ServiceType.parse(span.getServiceType());
        this.endPoint = span.getEndPoint();
        this.flag = span.getFlag();

        setAnnotationList(span.getAnnotations());
    }

    public SpanBo(long mostTraceId, long leastTraceId, long startTime, int elapsed, long spanId) {
        this.mostTraceId = mostTraceId;
        this.leastTraceId = leastTraceId;

        this.startTime = startTime;
        this.elapsed = elapsed;

        this.spanId = spanId;
    }

    public SpanBo() {
    }

    public int getVersion() {
        return version & 0xFF;
    }

    public void setVersion(int version) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("out of range (0~255)");
        }
        // range 체크
        this.version = (byte) (version & 0xFF);
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getMostTraceId() {
        return mostTraceId;
    }

    public void setMostTraceId(long mostTraceId) {
        this.mostTraceId = mostTraceId;
    }

    public long getLeastTraceId() {
        return leastTraceId;
    }

    public void setLeastTraceId(long leastTraceId) {
        this.leastTraceId = leastTraceId;
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public long getSpanId() {
        return spanId;
    }

    public void setSpanID(long spanId) {
        this.spanId = spanId;
    }

    public long getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(long parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(short flag) {
        this.flag = flag;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public List<AnnotationBo> getAnnotationBoList() {
        if (annotationBoList == null) {
            return Collections.emptyList();
        }
        return annotationBoList;
    }

    public void setAnnotationList(List<Annotation> anoList) {
        List<AnnotationBo> boList = new ArrayList<AnnotationBo>(anoList.size());
        for (Annotation ano : anoList) {
            boList.add(new AnnotationBo(ano));
        }
        this.annotationBoList = boList;
    }

    public void setAnnotationBoList(List<AnnotationBo> anoList) {
        // List<AnnotationBo> boList = new
        // ArrayList<AnnotationBo>(anoList.size());
        // for(Annotation ano : anoList) {
        // boList.add(new AnnotationBo(ano));
        // }
        // this.annotationBoList = boList;
        if (anoList == null) {
            this.annotationBoList = Collections.emptyList();
        } else {
            this.annotationBoList = anoList;
        }
    }

    public int increaseRecursiveCallCount() {
        return recursiveCallCount++;
    }

    public int getRecursiveCallCount() {
        return recursiveCallCount;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    private int getBufferLength(int a, int b, int c, int d) {
        int size = a + b + c + d;
        size += 1 + 1 + 1 + 1 + VERSION_SIZE; // chunk size chunk
        // size = size + TIMESTAMP + MOSTTRACEID + LEASTTRACEID + SPANID +
        // PARENTSPANID + FLAG + TERMINAL;
        size += PARENTSPANID + FLAG + SERVICETYPE;

        // startTime 8, elapsed 4;
        size += 12;
        return size;
    }

    public byte[] writeValue() {
        byte[] agentIDBytes = BytesUtils.getBytes(agentId);
        byte[] rpcBytes = BytesUtils.getBytes(rpc);
        byte[] serviceNameBytes = BytesUtils.getBytes(serviceName);
        byte[] endPointBytes = BytesUtils.getBytes(endPoint);

        int bufferLength = getBufferLength(agentIDBytes.length, rpcBytes.length, serviceNameBytes.length, endPointBytes.length);

        Buffer buffer = new Buffer(bufferLength);

        buffer.put(version);

        // buffer.put(mostTraceID);
        // buffer.put(leastTraceID);

        buffer.put1PrefixedBytes(agentIDBytes);

        // buffer.put(spanID);
        buffer.put(parentSpanId);

        buffer.put(startTime);
        buffer.put(elapsed);

        buffer.put1PrefixedBytes(rpcBytes);
        buffer.put1PrefixedBytes(serviceNameBytes);
        buffer.put(serviceType.getCode());
        buffer.put1PrefixedBytes(endPointBytes);

        buffer.put(flag);
        return buffer.getBuffer();
    }

    public int readValue(byte[] bytes, int offset) {
        Buffer buffer = new Buffer(bytes, offset);

        this.version = buffer.readByte();

        // this.mostTraceID = buffer.readLong();
        // this.leastTraceID = buffer.readLong();

        this.agentId = buffer.read1PrefixedString();

        // this.spanID = buffer.readLong();
        this.parentSpanId = buffer.readLong();

        this.startTime = buffer.readLong();
        this.elapsed = buffer.readInt();

        this.rpc = buffer.read1UnsignedPrefixedString();
        this.serviceName = buffer.read1UnsignedPrefixedString();
        this.serviceType = ServiceType.parse(buffer.readShort());
        this.endPoint = buffer.read1UnsignedPrefixedString();

        this.flag = buffer.readShort();
        return buffer.getOffset();
    }

    @Override
    public String toString() {
        return "SpanBo{" + "agentId='" + agentId + '\'' + ", startTime=" + startTime + ", elapsed=" + elapsed + ", mostTraceId=" + mostTraceId + ", leastTraceId=" + leastTraceId + ", rpc='" + rpc + '\'' + ", serviceName='" + serviceName + '\'' + ", spanID=" + spanId + ", parentSpanId=" + parentSpanId + ", flag=" + flag + ", endPoint='" + endPoint + "}";
    }
}