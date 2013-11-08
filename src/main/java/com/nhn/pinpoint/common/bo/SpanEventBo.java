package com.nhn.pinpoint.common.bo;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.OffsetFixedBuffer;
import com.nhn.pinpoint.common.util.TransactionId;
import com.nhn.pinpoint.common.util.TransactionIdUtils;
import com.nhn.pinpoint.thrift.dto.*;
import com.nhn.pinpoint.common.buffer.Buffer;

/**
 * @author emeroad
 */
public class SpanEventBo implements Span {
	private static final int VERSION_SIZE = 1;
	// version 0 = prefix의 사이즈를 int로

	private byte version = 0;

	private String agentId;
    private String applicationId;
    private long agentStartTime;

    private String traceAgentId;
	private long traceAgentStartTime;
	private long traceTransactionSequence;

	private long spanId;
	private short sequence;

	private int startElapsed;
	private int endElapsed;

	private String rpc;
	private ServiceType serviceType;

    private String destinationId;
	private String endPoint;
    private int apiId;

	private List<AnnotationBo> annotationBoList;

	private int depth = -1;
	private long nextSpanId = -1;

    private boolean hasException;
    private int exceptionId;
    private String exceptionMessage;
    // dao에서 찾아야 함.
    private String exceptionClass;


	public SpanEventBo() {
	}

	public SpanEventBo(TSpan tSpan, TSpanEvent tSpanEvent) {
        if (tSpan == null) {
            throw new NullPointerException("tSpan must not be null");
        }
        if (tSpanEvent == null) {
            throw new NullPointerException("tSpanEvent must not be null");
        }

        this.agentId = tSpan.getAgentId();
        this.applicationId = tSpan.getApplicationName();
        this.agentStartTime = tSpan.getAgentStartTime();

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(tSpan.getTransactionId());
        this.traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            traceAgentId = this.agentId;
        }
        this.traceAgentStartTime = transactionId.getAgentStartTime();
        this.traceTransactionSequence = transactionId.getTransactionSequence();

		this.spanId = tSpan.getSpanId();
		this.sequence = tSpanEvent.getSequence();

		this.startElapsed = tSpanEvent.getStartElapsed();
		this.endElapsed = tSpanEvent.getEndElapsed();

		this.rpc = tSpanEvent.getRpc();
		this.serviceType = ServiceType.findServiceType(tSpanEvent.getServiceType());


        this.destinationId = tSpanEvent.getDestinationId();

        this.endPoint = tSpanEvent.getEndPoint();
        this.apiId = tSpanEvent.getApiId();
		
		if (tSpanEvent.isSetDepth()) {
			this.depth = tSpanEvent.getDepth();
		}
        
		if (tSpanEvent.isSetNextSpanId()) {
			this.nextSpanId = tSpanEvent.getNextSpanId();
		}
        
		setAnnotationBoList(tSpanEvent.getAnnotations());

        final TIntStringValue exceptionInfo = tSpanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            this.hasException = true;
            this.exceptionId = exceptionInfo.getIntValue();
            this.exceptionMessage = exceptionInfo.getStringValue();
        }
	}

	public SpanEventBo(TSpanChunk spanChunk, TSpanEvent spanEvent) {
        if (spanChunk == null) {
            throw new NullPointerException("spanChunk must not be null");
        }
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }

        this.agentId = spanChunk.getAgentId();
        this.applicationId = spanChunk.getApplicationName();
        this.agentStartTime = spanChunk.getAgentStartTime();

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(spanChunk.getTransactionId());
        this.traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            traceAgentId = this.agentId;
        }
        this.traceAgentStartTime = transactionId.getAgentStartTime();
        this.traceTransactionSequence = transactionId.getTransactionSequence();

		this.spanId = spanChunk.getSpanId();
		this.sequence = spanEvent.getSequence();

		this.startElapsed = spanEvent.getStartElapsed();
		this.endElapsed = spanEvent.getEndElapsed();

		this.rpc = spanEvent.getRpc();
		this.serviceType = ServiceType.findServiceType(spanEvent.getServiceType());

        this.destinationId = spanEvent.getDestinationId();

		this.endPoint = spanEvent.getEndPoint();
        this.apiId = spanEvent.getApiId();
		
		if (spanEvent.isSetDepth()) {
			this.depth = spanEvent.getDepth();
		}

		if (spanEvent.isSetNextSpanId()) {
			this.nextSpanId = spanEvent.getNextSpanId();
		}
		
		setAnnotationBoList(spanEvent.getAnnotations());

        final TIntStringValue exceptionInfo = spanEvent.getExceptionInfo();
        if (exceptionInfo != null) {
            this.hasException = true;
            this.exceptionId = exceptionInfo.getIntValue();
            this.exceptionMessage = exceptionInfo.getStringValue();
        }
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

    public long getAgentStartTime() {
        return this.agentStartTime;
    }

    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
    }

    public String getTraceAgentId() {
        return traceAgentId;
    }

    public void setTraceAgentId(String traceAgentId) {
        this.traceAgentId = traceAgentId;
    }

    public long getTraceAgentStartTime() {
		return traceAgentStartTime;
	}

	public void setTraceAgentStartTime(long traceAgentStartTime) {
		this.traceAgentStartTime = traceAgentStartTime;
	}

	public long getTraceTransactionSequence() {
		return traceTransactionSequence;
	}

	public void setTraceTransactionSequence(long traceTransactionSequence) {
		this.traceTransactionSequence = traceTransactionSequence;
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

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }


    public List<AnnotationBo> getAnnotationBoList() {
		return annotationBoList;
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

	private void setAnnotationBoList(List<TAnnotation> annotations) {
        if (annotations == null) {
            return;
        }
		List<AnnotationBo> boList = new ArrayList<AnnotationBo>(annotations.size());
		for (TAnnotation ano : annotations) {
			boList.add(new AnnotationBo(ano));
		}
		this.annotationBoList = boList;
	}

    public boolean hasException() {
        return hasException;
    }

    public int getExceptionId() {
        return exceptionId;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }



    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer(512);

        buffer.put(version);

        // buffer.put(mostTraceID);
        // buffer.put(leastTraceID);

        buffer.putPrefixedString(agentId);
        buffer.putPrefixedString(applicationId);
        buffer.putVar(agentStartTime);

        buffer.putVar(startElapsed);
        buffer.putVar(endElapsed);
        // Qualifier에서 읽어서 set하므로 필요 없음.
        // buffer.put(sequence);

        buffer.putPrefixedString(rpc);
        buffer.put(serviceType.getCode());
        buffer.putPrefixedString(endPoint);
        buffer.putPrefixedString(destinationId);
        buffer.putSVar(apiId);

        buffer.putSVar(depth);
        buffer.put(nextSpanId);

        if (hasException) {
            buffer.put(true);
            buffer.putSVar(exceptionId);
            buffer.putPrefixedString(exceptionMessage);
        } else {
            buffer.put(false);
        }

        writeAnnotation(buffer);


        return buffer.getBuffer();
    }



    private void writeAnnotation(Buffer buffer) {
        AnnotationBoList annotationBo = new AnnotationBoList(this.annotationBoList);
        annotationBo.writeValue(buffer);
	}


	public int readValue(byte[] bytes, int offset) {
        final Buffer buffer = new OffsetFixedBuffer(bytes, offset);

		this.version = buffer.readByte();

		// this.mostTraceID = buffer.readLong();
		// this.leastTraceID = buffer.readLong();

		this.agentId = buffer.readPrefixedString();
        this.applicationId = buffer.readPrefixedString();
        this.agentStartTime = buffer.readVarLong();

		this.startElapsed = buffer.readVarInt();
		this.endElapsed = buffer.readVarInt();
		// Qualifier에서 읽어서 가져오므로 하지 않아도 됨.
		// this.sequence = buffer.readShort();


		this.rpc = buffer.readPrefixedString();
		this.serviceType = ServiceType.findServiceType(buffer.readShort());
		this.endPoint = buffer.readPrefixedString();
        this.destinationId = buffer.readPrefixedString();
        this.apiId = buffer.readSVarInt();

		this.depth = buffer.readSVarInt();
		this.nextSpanId = buffer.readLong();

        this.hasException = buffer.readBoolean();
        if (hasException) {
            this.exceptionId = buffer.readSVarInt();
            this.exceptionMessage = buffer.readPrefixedString();
        }
		
		this.annotationBoList = readAnnotation(buffer);
		return buffer.getOffset();
	}

	private List<AnnotationBo> readAnnotation(Buffer buffer) {
        AnnotationBoList annotationBoList = new AnnotationBoList();
        annotationBoList.readValue(buffer);
		return annotationBoList.getAnnotationBoList();
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("SpanEventBo{");
        sb.append("version=").append(version);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", applicationId='").append(applicationId).append('\'');
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", traceAgentId='").append(traceAgentId).append('\'');
        sb.append(", traceAgentStartTime=").append(traceAgentStartTime);
        sb.append(", traceTransactionSequence=").append(traceTransactionSequence);
        sb.append(", spanId=").append(spanId);
        sb.append(", sequence=").append(sequence);
        sb.append(", startElapsed=").append(startElapsed);
        sb.append(", endElapsed=").append(endElapsed);
        sb.append(", rpc='").append(rpc).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", destinationId='").append(destinationId).append('\'');
        sb.append(", endPoint='").append(endPoint).append('\'');
        sb.append(", apiId=").append(apiId);
        sb.append(", annotationBoList=").append(annotationBoList);
        sb.append(", depth=").append(depth);
        sb.append(", nextSpanId=").append(nextSpanId);
        sb.append(", hasException=").append(hasException);
        sb.append(", exceptionId=").append(exceptionId);
        sb.append(", exceptionMessage='").append(exceptionMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
