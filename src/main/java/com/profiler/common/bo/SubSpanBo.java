package com.profiler.common.bo;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.common.util.Buffer;
import com.profiler.common.util.BytesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SubSpanBo {
    private static final int VERSION_SIZE = 1;
    // version 0 = prefix의 사이즈를 int로

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
    private short sequence;

    private int startElapsed;
    private int endElapsed;

    private String rpc;
    private String serviceName;
    private ServiceType serviceType;
    private String endPoint;
    private List<AnnotationBo> annotationBoList;

    public SubSpanBo() {
    }

    public SubSpanBo(SubSpan subSpan) {
        this.agentId = subSpan.getAgentId();

        this.mostTraceId = subSpan.getMostTraceId();
        this.leastTraceId = subSpan.getLeastTraceId();

        this.spanId = subSpan.getSpanId();
        this.sequence = subSpan.getSequence();

        this.startElapsed = subSpan.getStartElapsed();
        this.endElapsed = subSpan.getEndElapsed();

        this.rpc = subSpan.getRpc();
        this.serviceName = subSpan.getServiceName();
        this.serviceType = ServiceType.parse(subSpan.getServiceType());

        this.endPoint = subSpan.getEndPoint();
        setAnnotationBoList(subSpan.getAnnotations());
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
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

    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public long getSpanId() {
        return this.spanId;
    }

    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }

    public int getStartElapsed() {
        return startElapsed;
    }

    public void setStartElapsed(int startElapsed) {
        this.startElapsed = startElapsed;
    }

    public int getEndElapsed() {
        return endElapsed;
    }

    public void setEndElapsed(int endElapsed) {
        this.endElapsed = endElapsed;
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

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public List<AnnotationBo> getAnnotationBoList() {
        return annotationBoList;
    }

    private void setAnnotationBoList(List<Annotation> annotations) {
        List<AnnotationBo> boList = new ArrayList<AnnotationBo>(annotations.size());
        for (Annotation ano : annotations) {
            boList.add(new AnnotationBo(ano));
        }
        this.annotationBoList = boList;
    }

    private int getBufferLength(int a, int b, int c, int d) {
        int size = a + b + c + d;
        size += 1 + 1 + 1 + 1 + VERSION_SIZE; // chunk size chunk
        // size = size + TIMESTAMP + MOSTTRACEID + LEASTTRACEID + SPANID +
        // PARENTSPANID + FLAG + TERMINAL;
        size += SERVICETYPE;

        // startTime 8, elapsed 4;
        size += 8;
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

        buffer.put(startElapsed);
        buffer.put(endElapsed);

        buffer.put1PrefixedBytes(rpcBytes);
        buffer.put1PrefixedBytes(serviceNameBytes);
        buffer.put(serviceType.getCode());
        buffer.put1PrefixedBytes(endPointBytes);


        return buffer.getBuffer();
    }

    public int readValue(byte[] bytes, int offset) {
        Buffer buffer = new Buffer(bytes, offset);

        this.version = buffer.readByte();

        // this.mostTraceID = buffer.readLong();
        // this.leastTraceID = buffer.readLong();

        this.agentId = buffer.read1PrefixedString();


        this.startElapsed = buffer.readInt();
        this.endElapsed = buffer.readInt();

        this.rpc = buffer.read1UnsignedPrefixedString();
        this.serviceName = buffer.read1UnsignedPrefixedString();
        this.serviceType = ServiceType.parse(buffer.readShort());
        this.endPoint = buffer.read1UnsignedPrefixedString();

        return buffer.getOffset();
    }

    @Override
    public String toString() {
        return "SubSpanBo{" + "agentId='" + agentId + '\'' + ", startElapsed=" + startElapsed + ", endElapsed=" + endElapsed
                + ", mostTraceId=" + mostTraceId + ", leastTraceId=" + leastTraceId + ", rpc='" + rpc + '\''
                + ", serviceName='" + serviceName + '\'' + ", spanID=" + spanId + ", sequence=" + sequence
                + ", endPoint='" + endPoint + ", serviceType=" + serviceType + "}";
    }
}
