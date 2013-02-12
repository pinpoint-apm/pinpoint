package com.profiler.common.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.buffer.Buffer;
import com.profiler.common.util.BytesUtils;
import com.profiler.common.buffer.FixedBuffer;

/**
 *
 */
public class SpanBo implements com.profiler.common.bo.Span {

    private static final int VERSION_SIZE = 1;
    // version 0 = prefix의 사이즈를 int로

    private byte version = 0;

    // private static final int MOSTTRACEID = 8;
    // private static final int LEASTTRACEID = 8;
    // private static final int SPANID = 4;
    private static final int PARENTSPANID = 4;

    // private static final int TIMESTAMP = 8;
    private static final int SERVICETYPE = 2;
    private static final int FLAG = 2;
    private static final int AGENTIDENTIFIER = 2;

    private String agentId;
    private short agentIdentifier;
    private long mostTraceId;
    private long leastTraceId;
    private int spanId;
    private int parentSpanId;
    private long startTime;
    private int elapsed;
    private String rpc;
    private String serviceName;
    private ServiceType serviceType;
    private String endPoint;
    private List<AnnotationBo> annotationBoList;
    private short flag; // optional

    private List<SubSpanBo> subSpanList;

    private int recursiveCallCount = 0;

    private boolean exception = false;

	public SpanBo(Span span) {
        this.agentId = span.getAgentId();
        this.agentIdentifier = span.getAgentIdentifier();

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

        this.exception = span.isErr();
        
        setAnnotationList(span.getAnnotations());
    }

    public SpanBo(long mostTraceId, long leastTraceId, long startTime, int elapsed, int spanId) {
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

	public String getTraceId() {
		return new UUID(mostTraceId, leastTraceId).toString();
	}
    
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public short getAgentIdentifier() {
        return agentIdentifier;
    }

    public void setAgentIdentifier(short agentIdentifier) {
        this.agentIdentifier = agentIdentifier;
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
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

    public int getSpanId() {
        return spanId;
    }

    public void setSpanID(int spanId) {
        this.spanId = spanId;
    }

    public int getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(int parentSpanId) {
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

    public void addSubSpan(SubSpanBo subSpan) {
        if (subSpanList == null) {
            subSpanList = new ArrayList<SubSpanBo>();
        }
        subSpanList.add(subSpan);
    }

    public List<SubSpanBo> getSubSpanList() {
        return subSpanList;
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
    
    public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}

    private int getBufferLength(int a, int b, int c, int d) {
        int size = a + b + c + d;
        size += 1 + 1 + 1 + 1 + 1 + VERSION_SIZE; // chunk size chunk
        // size = size + TIMESTAMP + MOSTTRACEID + LEASTTRACEID + SPANID +
        // PARENTSPANID + FLAG + TERMINAL;
        size += PARENTSPANID + SERVICETYPE + AGENTIDENTIFIER;
        if (flag != 0) {
            size += FLAG;
        }
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

        Buffer buffer = new FixedBuffer(bufferLength);

        buffer.put(version);

        // buffer.put(mostTraceID);
        // buffer.put(leastTraceID);

        buffer.put1PrefixedBytes(agentIDBytes);
        buffer.put(agentIdentifier);

        // buffer.put(spanID);
        buffer.put(parentSpanId);

        buffer.put(startTime);
        buffer.put(elapsed);

        buffer.put1PrefixedBytes(rpcBytes);
        buffer.put1PrefixedBytes(serviceNameBytes);
        buffer.put(serviceType.getCode());
        buffer.put1PrefixedBytes(endPointBytes);
        
        buffer.put(exception);
        
        // 공간 절약을 위해서 flag는 무조껀 마지막에 넣어야 한다.
        if (flag != 0) {
            buffer.put(flag);
        }

        return buffer.getBuffer();
    }

    public int readValue(byte[] bytes, int offset) {
        Buffer buffer = new FixedBuffer(bytes, offset);

        this.version = buffer.readByte();

        // this.mostTraceID = buffer.readLong();
        // this.leastTraceID = buffer.readLong();

        this.agentId = buffer.read1PrefixedString();
        this.agentIdentifier = buffer.readShort();

        // this.spanID = buffer.readLong();
        this.parentSpanId = buffer.readInt();

        this.startTime = buffer.readLong();
        this.elapsed = buffer.readInt();

        this.rpc = buffer.read1UnsignedPrefixedString();
        this.serviceName = buffer.read1UnsignedPrefixedString();
        this.serviceType = ServiceType.parse(buffer.readShort());
        this.endPoint = buffer.read1UnsignedPrefixedString();
        
        this.exception = buffer.readBoolean();
        
        // flag는 무조껀 마지막에 넣어야 한다.
        if (buffer.limit() == 2) {
            this.flag = buffer.readShort();
        }
        return buffer.getOffset();
    }

    @Override
    public String toString() {
		return "SpanBo{" + "agentId='" + agentId + '\'' + ", startTime=" + startTime + ", elapsed=" + elapsed + ", mostTraceId=" + mostTraceId + ", leastTraceId=" + leastTraceId + ", rpc='" + rpc + '\'' + ", serviceName='" + serviceName + '\'' + ", spanID=" + spanId + ", parentSpanId=" + parentSpanId + ", flag=" + flag + ", endPoint='" + endPoint + ", serviceType=" + serviceType + ", subSpans=" + subSpanList + "}";
    }
}