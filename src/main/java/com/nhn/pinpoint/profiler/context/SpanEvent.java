package com.nhn.pinpoint.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.AgentKey;
import com.nhn.pinpoint.thrift.dto.Annotation;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class SpanEvent implements Thriftable {

    private final Span parentSpan;

    private short sequence;

    private long startTime;
    private long endTime;
    private String rpc;
    private ServiceType serviceType;

    private String endPoint;

    private String destionationId;
    private List<String> destinationAddress;

    private final List<TraceAnnotation> traceAnnotationList = new ArrayList<TraceAnnotation>(4);

    private int nextSpanId = -1;
    private int depth = -1;
    
    public SpanEvent(Span parentSpan) {
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

    public boolean addAnnotation(TraceAnnotation traceAnnotation) {
        return traceAnnotationList.add(traceAnnotation);
    }

    public List<TraceAnnotation> getTraceAnnotationList() {
        return traceAnnotationList;
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

    public String getDestionationId() {
        return destionationId;
    }

    public void setDestionationId(String destionationId) {
        this.destionationId = destionationId;
    }

    public List<String> getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(List<String> destinationAddress) {
        this.destinationAddress = destinationAddress;
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
    

	
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getNextSpanId() {
		return nextSpanId;
	}

	public void setNextSpanId(int nextSpanId) {
		this.nextSpanId = nextSpanId;
	}

	public String toString() {
        StringBuilder sb = new StringBuilder(256);

        sb.append("{");
        sb.append("\n\t Depth = ").append(depth);
        sb.append("\n\t NextSpanid=").append(nextSpanId);
        sb.append("\n\t ParentTraceID=").append(parentSpan.getTraceID());
        sb.append("\n\t Sequence=").append(sequence);
        sb.append(",\n\t StartTime=").append(startTime);
        sb.append(", EndTime=").append(endTime);
        sb.append(",\n\t Name=").append(rpc);
        sb.append(", ServiceType=").append(serviceType);
        sb.append(", EndPoint=").append(endPoint);
        sb.append(", Seq=").append(sequence);
        sb.append(",\n\t Annotations = {");
        for (TraceAnnotation a : traceAnnotationList) {
            sb.append("\n\t\t").append(a);
        }
        sb.append("\n\t}");

        sb.append("}");

        return sb.toString();
    }

    public com.nhn.pinpoint.thrift.dto.SpanEvent toThrift() {
        return toThrift(false);
    }

    public com.nhn.pinpoint.thrift.dto.SpanEvent toThrift(boolean child) {
        com.nhn.pinpoint.thrift.dto.SpanEvent spanEvent = new com.nhn.pinpoint.thrift.dto.SpanEvent();

        long parentSpanStartTime = parentSpan.getStartTime();
        spanEvent.setStartElapsed((int) (startTime - parentSpanStartTime));
        spanEvent.setEndElapsed((int) (endTime - startTime));

        spanEvent.setSequence(sequence);
        // Span내부의 SpanEvent로 들어가지 않을 경우
        if (!child) {
            AgentKey agentKey = new AgentKey();
            DefaultAgent agent = DefaultAgent.getInstance();
            agentKey.setAgentId(agent.getAgentId());
            agentKey.setApplicationName(agent.getApplicationName());
            agentKey.setAgentStartTime(agent.getStartTime());

            spanEvent.setAgentKey(agentKey);

            spanEvent.setParentServiceType(parentSpan.getServiceType().getCode()); // added
            spanEvent.setParentEndPoint(parentSpan.getEndPoint()); // added

            TraceID parentSpanTraceID = parentSpan.getTraceID();
            spanEvent.setMostTraceId(parentSpanTraceID.getId().getMostSignificantBits());
            spanEvent.setLeastTraceId(parentSpanTraceID.getId().getLeastSignificantBits());
            spanEvent.setSpanId(parentSpanTraceID.getSpanId());
        }

        spanEvent.setRpc(rpc);
		spanEvent.setServiceType(serviceType.getCode());

        spanEvent.setEndPoint(endPoint);
        spanEvent.setDestinationId(this.destionationId);

        // 여기서 데이터 인코딩을 하자.
        List<Annotation> annotationList = new ArrayList<Annotation>(traceAnnotationList.size());
        for (TraceAnnotation traceAnnotation : traceAnnotationList) {
            annotationList.add(traceAnnotation.toThrift());
        }
        spanEvent.setAnnotations(annotationList);

		if (depth != -1) {
			spanEvent.setDepth(depth);
		}

		if (nextSpanId != -1) {
			spanEvent.setNextSpanId(nextSpanId);
		}
        
        return spanEvent;
    }

    public void setDestinationAddress() {
        //To change body of created methods use File | Settings | File Templates.
    }
}
