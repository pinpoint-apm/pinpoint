package com.nhn.pinpoint.context;

import com.nhn.pinpoint.DefaultAgent;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.dto.thrift.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class Span implements Thriftable {

    private final TraceID traceID;
    private long startTime;
    private long endTime;

    private String rpc;
    private ServiceType serviceType;
    private String endPoint;

    private int exception;
    private String remoteAddr;

    private final List<TraceAnnotation> traceAnnotationList = new ArrayList<TraceAnnotation>(5);

    private List<SpanEvent> spanEventList;
    
    // 아래 두 개 변수는 서버맵을 통계 데이터로 그리기 위한 용도.
    private String parentApplicationName = null;
    private short parentApplicationType = -1;
    private String acceptorHost = null;
    
    public Span(TraceID traceId) {
        this.traceID = traceId;
    }

    public TraceID getTraceID() {
        return traceID;
    }

    public boolean addAnnotation(TraceAnnotation traceAnnotation) {
        return traceAnnotationList.add(traceAnnotation);
    }

    public int getAnnotationSize() {
        return traceAnnotationList.size();
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public List<SpanEvent> getSpanEventList() {
        return spanEventList;
    }

    public void setSpanEventList(List<SpanEvent> spanEventList) {
        this.spanEventList = spanEventList;
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

	public String getParentApplicationName() {
		return parentApplicationName;
	}

	public void setParentApplicationName(String parentApplicationName) {
		this.parentApplicationName = parentApplicationName;
	}
	
	public String getAcceptorHost() {
		return acceptorHost;
	}

	public void setAcceptorHost(String acceptorHost) {
		this.acceptorHost = acceptorHost;
	}
	
	public short getParentApplicationType() {
		return parentApplicationType;
	}

	public void setParentApplicationType(short parentApplicationType) {
		this.parentApplicationType = parentApplicationType;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(128);

        sb.append("{");
        sb.append("\n\t TraceID = ").append(traceID);
        sb.append(",\n\t StartTime = ").append(startTime);
        sb.append(", EndTime = ").append(endTime);
        sb.append(",\n\t Name = ").append(rpc);
        sb.append(", ServiceType = ").append(serviceType);
        sb.append(", EndPoint = ").append(endPoint);
        sb.append(", Exception = ").append(exception);
        sb.append(", RemoteAddr = ").append(remoteAddr);
        sb.append(", ParentApplication = ").append(parentApplicationName);
        sb.append(", ParentApplicationType = ").append(ServiceType.findServiceType(parentApplicationType));
        sb.append(", AcceptorHost = ").append(acceptorHost);
        sb.append(",\n\t Annotations = {");
        for (TraceAnnotation a : traceAnnotationList) {
            sb.append("\n\t\t").append(a);
        }
        sb.append("\n\t}");

        sb.append("}");

        return sb.toString();
    }

    public com.nhn.pinpoint.common.dto.thrift.Span toThrift() {
        com.nhn.pinpoint.common.dto.thrift.Span span = new com.nhn.pinpoint.common.dto.thrift.Span();

        span.setAgentId(DefaultAgent.getInstance().getAgentId());
        span.setApplicationName(DefaultAgent.getInstance().getApplicationName());
        span.setAgentStartTime(DefaultAgent.getInstance().getStartTime());

        span.setStartTime(startTime);
        span.setElapsed((int) (endTime - startTime));
        span.setMostTraceId(traceID.getId().getMostSignificantBits());
        span.setLeastTraceId(traceID.getId().getLeastSignificantBits());
        span.setRpc(rpc);
        span.setServiceType(serviceType.getCode());
        span.setSpanId(traceID.getSpanId());
        final int parentSpanId = traceID.getParentSpanId();
        if (parentSpanId != SpanID.NULL) {
            span.setParentSpanId(parentSpanId);
        }
        span.setEndPoint(endPoint);
        span.setRemoteAddr(remoteAddr);
        if (exception != 0) {
            span.setErr(exception);
        }

        if (parentApplicationName != null) {
        	span.setParentApplicationName(parentApplicationName);
        	span.setParentApplicationType(parentApplicationType);
        }
        
        if (acceptorHost != null) {
        	span.setAcceptorHost(acceptorHost);
        }
        
        // 여기서 데이터 인코딩을 하자.
        List<Annotation> annotationList = new ArrayList<Annotation>(traceAnnotationList.size());
        for (TraceAnnotation traceAnnotation : traceAnnotationList) {
            annotationList.add(traceAnnotation.toThrift());
        }
        span.setAnnotations(annotationList);

        span.setFlag(traceID.getFlags());

        List<SpanEvent> spanEventList = this.getSpanEventList();
        if (spanEventList != null && spanEventList.size() != 0) {

            List<com.nhn.pinpoint.common.dto.thrift.SpanEvent> tSpanEventList = new ArrayList<com.nhn.pinpoint.common.dto.thrift.SpanEvent>(spanEventList.size());
            for (SpanEvent spanEvent : spanEventList) {
                com.nhn.pinpoint.common.dto.thrift.SpanEvent tSpanEvent = spanEvent.toThrift(true);
                tSpanEventList.add(tSpanEvent);
            }
            span.setSpanEventList(tSpanEventList);
        }

        return span;
    }
}
