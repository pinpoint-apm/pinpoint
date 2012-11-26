package com.profiler.common.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private String agentId; // required

    // private static final int TIMESTAMP = 8;
    private long startTime; // required
    private int elapsed; // required

    // private static final int MOSTTRACEID = 8;
    private long mostTraceId; // required

    // private static final int LEASTTRACEID = 8;
    private long leastTraceId; // required

    private String name; // required
    private String serviceName; // required

    // private static final int SPANID = 8;
    private long spanId; // required

    private static final int PARENTSPANID = 8;
    private long parentSpanId; // optional

    private static final int FLAG = 4;
    private int flag; // optional
    // private List<Annotation> annotations; // required

    private String endPoint; // required

    private static final int TERMINAL = 1;
    private boolean terminal; // required

    private int recursiveCallCount = 0;

    private List<AnnotationBo> annotationBoList;

    public SpanBo(Span span) {
        this.agentId = span.getAgentId();
        this.startTime = span.getStartTime();
        this.elapsed = span.getElapsed();
        this.mostTraceId = span.getMostTraceId();
        this.leastTraceId = span.getLeastTraceId();
        this.name = span.getName();
        this.serviceName = span.getServiceName();
        this.spanId = span.getSpanId();
        this.parentSpanId = span.getParentSpanId();
        this.endPoint = span.getEndPoint();
        this.flag = span.getFlag();
        this.terminal = span.isTerminal();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
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

    private int getBufferLength(int a, int b, int c, int d) {
        int size = a + b + c + d;
        size = size + (4 * 4) + VERSION_SIZE; // chunk
        // size = size + TIMESTAMP + MOSTTRACEID + LEASTTRACEID + SPANID +
        // PARENTSPANID + FLAG + TERMINAL;
        size = size + PARENTSPANID + FLAG + TERMINAL;
        // startTime, endTime;
        size += 16;
        return size;
    }

    public byte[] writeValue() {
        byte[] agentIDBytes = BytesUtils.getBytes(agentId);
        byte[] nameBytes = BytesUtils.getBytes(name);
        byte[] serviceNameBytes = BytesUtils.getBytes(serviceName);
        byte[] endPointBytes = BytesUtils.getBytes(endPoint);
        int bufferLength = getBufferLength(agentIDBytes.length, nameBytes.length, serviceNameBytes.length, endPointBytes.length);

        Buffer buffer = new Buffer(bufferLength);
        buffer.put(version);
        buffer.putPrefixedBytes(agentIDBytes);
        buffer.put(startTime);
        buffer.put(elapsed);
        // buffer.put(leastTraceID);
        buffer.putPrefixedBytes(nameBytes);
        buffer.putPrefixedBytes(serviceNameBytes);
        // buffer.put(spanID);
        buffer.put(parentSpanId);
        buffer.put(flag);
        buffer.putPrefixedBytes(endPointBytes);
        buffer.put(terminal);
        return buffer.getBuffer();
    }

    public int readValue(byte[] bytes, int offset) {
        Buffer buffer = new Buffer(bytes, offset);
        this.version = buffer.readByte();
        this.agentId = buffer.readPrefixedString();
        this.startTime = buffer.readLong();
        this.elapsed = buffer.readInt();
        // this.leastTraceID = buffer.readLong();
        this.name = buffer.readPrefixedString();
        this.serviceName = buffer.readPrefixedString();
        // this.spanID = buffer.readLong();
        this.parentSpanId = buffer.readLong();
        this.flag = buffer.readInt();
        this.endPoint = buffer.readPrefixedString();
        this.terminal = buffer.readBoolean();
        return buffer.getOffset();
    }

    public int increaseRecursiveCallCount() {
        return recursiveCallCount++;
    }

    public int getRecursiveCallCount() {
        return recursiveCallCount;
    }

    @Override
    public String toString() {
        return "SpanBo{" + "agentId='" + agentId + '\''
                + ", startTime=" + startTime + ", elapsed=" + elapsed + ", mostTraceId=" + mostTraceId
                + ", leastTraceId=" + leastTraceId + ", name='" + name + '\'' + ", serviceName='" + serviceName + '\'' + ", spanID=" + spanId + ", parentSpanId=" + parentSpanId + ", flag=" + flag + ", endPoint='" + endPoint + '\'' + ", terminal=" + terminal + '}';
    }
}