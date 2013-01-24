package com.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.profiler.Agent;
import com.profiler.common.ServiceType;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class SubSpan implements Thriftable {

    private final Span parentSpan;

    private short sequence;

    private long startTime;
    private long endTime;
    private String serviceName;
    private String rpc;
    private ServiceType serviceType;
    private String endPoint;
    private boolean exception;
    
    private final List<HippoAnnotation> annotations = new ArrayList<HippoAnnotation>(5);

    private Long nextSpanId = null;
    private Integer depth = null;
    
    public SubSpan(Span parentSpan) {
        this.parentSpan = parentSpan;
    }

    public Span getParentSpan() {
        return parentSpan;
    }

    public short getSequence() {
        return sequence;
    }

    public void setSequence(short sequence) {
        this.sequence = sequence;
    }

    public boolean addAnnotation(HippoAnnotation annotation) {
        return annotations.add(annotation);
    }

    public List<HippoAnnotation> getAnnotations() {
        return annotations;
    }

    public int getAnnotationSize() {
        return annotations.size();
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
    
    public boolean isException() {
		return exception;
	}

	public void setException(boolean exception) {
		this.exception = exception;
	}
	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public long getNextSpanId() {
		return nextSpanId;
	}

	public void setNextSpanId(long nextSpanId) {
		this.nextSpanId = nextSpanId;
	}

	public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\n\t Depth = ").append(depth);
        sb.append("\n\t NextSpanid=").append(nextSpanId);
        sb.append("\n\t ParentTraceID=").append(parentSpan.getTraceID());
        sb.append("\n\t Sequence=").append(sequence);
        sb.append(",\n\t StartTime=").append(startTime);
        sb.append(", EndTime=").append(endTime);
        sb.append(",\n\t Name=").append(rpc);
        sb.append(", ServiceName=").append(serviceName);
        sb.append(", ServiceType=").append(serviceType);
        sb.append(", EndPoint=").append(endPoint);
        sb.append(", Exception=").append(exception);
        sb.append(", Seq=").append(sequence);
        sb.append(",\n\t Annotations = {");
        for (HippoAnnotation a : annotations) {
            sb.append("\n\t\t").append(a);
        }
        sb.append("\n\t}");

        sb.append("}");

        return sb.toString();
    }

    public com.profiler.common.dto.thrift.SubSpan toThrift() {
        return toThrift(false);
    }

    public com.profiler.common.dto.thrift.SubSpan toThrift(boolean child) {
        com.profiler.common.dto.thrift.SubSpan subSpan = new com.profiler.common.dto.thrift.SubSpan();

        long parentSpanStartTime = parentSpan.getStartTime();
        subSpan.setStartElapsed((int) (startTime - parentSpanStartTime));
        subSpan.setEndElapsed((int) (endTime - startTime));

        subSpan.setSequence(sequence);
        // 다른 span의 sub로 들어가지 않을 경우
        if (!child) {
            subSpan.setAgentId(Agent.getInstance().getAgentId());
            TraceID parentSpanTraceID = parentSpan.getTraceID();
            subSpan.setMostTraceId(parentSpanTraceID.getId().getMostSignificantBits());
            subSpan.setLeastTraceId(parentSpanTraceID.getId().getLeastSignificantBits());
            subSpan.setSpanId(parentSpanTraceID.getSpanId());
        }

        subSpan.setRpc(rpc);
        subSpan.setServiceName(serviceName);
        
		if (serviceType != null) {
			subSpan.setServiceType(serviceType.getCode());
		}
		
        subSpan.setEndPoint(endPoint);
        subSpan.setErr(exception);

        // 여기서 데이터 인코딩을 하자.
        List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(annotations.size());
        for (HippoAnnotation a : annotations) {
            annotationList.add(a.toThrift());
        }
        subSpan.setAnnotations(annotationList);

		if (depth != null) {
			subSpan.setDepth(depth);
		}

		if (nextSpanId != null) {
			subSpan.setNextSpanId(nextSpanId);
		}
        
        return subSpan;
    }
}
