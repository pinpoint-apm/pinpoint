package com.nhn.pinpoint.common.bo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.dto2.thrift.Annotation;
import com.nhn.pinpoint.common.dto2.thrift.Span;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.util.TraceIdUtils;

/**
 *
 */
public class SpanBo implements com.nhn.pinpoint.common.bo.Span {

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
    private static final int AGENTSTARTTIME = 8;
    private static final int EXCEPTION_SIZE = 4;

//    private AgentKeyBo agentKeyBo;
    private String agentId;
    private String applicationId;
    private long agentStartTime;

    private long mostTraceId;
    private long leastTraceId;
    private int spanId;
    private int parentSpanId;

    private long startTime;
    private int elapsed;

    private String rpc;
    private ServiceType serviceType;
    private String endPoint;

    private List<AnnotationBo> annotationBoList;
    private short flag; // optional
    private int exception;

    private List<SpanEventBo> spanEventBoList;

    private long collectorAcceptTime;

    
    private String remoteAddr; // optional

	public SpanBo(Span span) {
        this.agentId = span.getAgentId();
        this.applicationId = span.getApplicationName();
        this.agentStartTime = span.getAgentStartTime();

        this.mostTraceId = span.getMostTraceId();
        this.leastTraceId = span.getLeastTraceId();

        this.spanId = span.getSpanId();
        this.parentSpanId = span.getParentSpanId();

        this.startTime = span.getStartTime();
        this.elapsed = span.getElapsed();

        this.rpc = span.getRpc();

        this.serviceType = ServiceType.findServiceType(span.getServiceType());
        this.endPoint = span.getEndPoint();
        this.flag = span.getFlag();

        this.exception = span.getErr();
        
        this.remoteAddr = span.getRemoteAddr();
        
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
        return TraceIdUtils.formatString(mostTraceId, leastTraceId);
	}
    
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
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
        if (anoList == null) {
            this.annotationBoList = Collections.emptyList();
        } else {
            this.annotationBoList = anoList;
        }
    }

    public void addSpanEvent(SpanEventBo spanEventBo) {
        if (spanEventBoList == null) {
            spanEventBoList = new ArrayList<SpanEventBo>();
        }
        spanEventBoList.add(spanEventBo);
    }

    public List<SpanEventBo> getSpanEventBoList() {
        return spanEventBoList;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    
    public int getException() {
		return exception;
	}

	public void setException(int exception) {
		this.exception = exception;
	}
	
    public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public long getCollectorAcceptTime() {
        return collectorAcceptTime;
    }

    public void setCollectorAcceptTime(long collectorAcceptTime) {
        this.collectorAcceptTime = collectorAcceptTime;
    }

    public boolean isRoot() {
    	return -1 == parentSpanId;
    }
    
//    private int getBufferLength(int a, int b, int c, int d, int e) {
//	    int size = a + b + c + d + e;
//	    size += 1 + 1 + 1 + 1 + 1 + VERSION_SIZE; // chunk size chunk
//	    // size = size + TIMESTAMP + MOSTTRACEID + LEASTTRACEID + SPANID +
//        // PARENTSPANID + FLAG + TERMINAL;
//        size += PARENTSPANID + SERVICETYPE + AGENTSTARTTIME + EXCEPTION_SIZE;
//        if (flag != 0) {
//            size += FLAG;
//        }
//        // startTime 8, elapsed 4;
//        size += 12;
//        return size;
//    }

    // io wirte시 variable encoding을 추가함.
    // 약 10%정도 byte 사이즈가 줄어드는 효과가 있음.
    public byte[] writeValue() {
        byte[] agentIDBytes = BytesUtils.getBytes(agentId);
        byte[] rpcBytes = BytesUtils.getBytes(rpc);
        byte[] applicationIdBytes = BytesUtils.getBytes(applicationId);
        byte[] endPointBytes = BytesUtils.getBytes(endPoint);
        byte[] remoteAddrBytes = BytesUtils.getBytes(remoteAddr);

        // var encoding 사용시 사이즈를 측정하기 어려움. 안되는것음 아님 편의상 그냥 자동 증가 buffer를 사용한다.
        // 향후 더 효율적으로 메모리를 사용하게 한다면 getBufferLength를 다시 부활 시키는것을 고려한다.
        Buffer buffer = new AutomaticBuffer(256);

        buffer.put(version);

        // buffer.put(mostTraceID);
        // buffer.put(leastTraceID);

        buffer.put1PrefixedBytes(agentIDBytes);
        // time의 경우도 현재 시간을 기준으로 var를 사용하는게 사이즈가 더 작음 6byte를 먹음.
        buffer.putVar(agentStartTime);

        // rowkey에 들어감.
        // buffer.put(spanID);
        buffer.put(parentSpanId);

        // 현재 시간이 기준이므로 var encoding
        buffer.putVar(startTime);
        buffer.putVar(elapsed);

        buffer.put1PrefixedBytes(rpcBytes);
        buffer.put1PrefixedBytes(applicationIdBytes);
        buffer.put(serviceType.getCode());
        buffer.put1PrefixedBytes(endPointBytes);
        buffer.put1PrefixedBytes(remoteAddrBytes);

        // exception code는 음수가 될수 있음.
        buffer.putSVar(exception);

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
        this.agentStartTime = buffer.readVarLong();

        // this.spanID = buffer.readLong();
        this.parentSpanId = buffer.readInt();

        this.startTime = buffer.readVarLong();
        this.elapsed = buffer.readVarInt();

        this.rpc = buffer.read1UnsignedPrefixedString();
        this.applicationId = buffer.read1UnsignedPrefixedString();
        this.serviceType = ServiceType.findServiceType(buffer.readShort());
        this.endPoint = buffer.read1UnsignedPrefixedString();
        this.remoteAddr = buffer.read1UnsignedPrefixedString();
        
        this.exception = buffer.readSVarInt();
        
        // flag는 무조껀 마지막에 넣어야 한다.
        if (buffer.limit() == 2) {
            this.flag = buffer.readShort();
        }
        return buffer.getOffset();
    }

    @Override
    public String toString() {
        return "SpanBo{" +
                "version=" + version +
                ", agentId='" + agentId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", mostTraceId=" + mostTraceId +
                ", leastTraceId=" + leastTraceId +
                ", spanId=" + spanId +
                ", parentSpanId=" + parentSpanId +
                ", startTime=" + startTime +
                ", elapsed=" + elapsed +
                ", rpc='" + rpc + '\'' +
                ", serviceType=" + serviceType +
                ", endPoint='" + endPoint + '\'' +
                ", annotationBoList=" + annotationBoList +
                ", flag=" + flag +
                ", exception=" + exception +
                ", spanEventBoList=" + spanEventBoList +
                ", collectorAcceptTime=" + collectorAcceptTime +
                ", remoteAddr='" + remoteAddr + '\'' +
                '}';
    }
}