package com.nhn.pinpoint.common.bo;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.OffsetFixedBuffer;
import com.nhn.pinpoint.common.util.TransactionId;
import com.nhn.pinpoint.common.util.TransactionIdUtils;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TIntStringValue;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.common.buffer.Buffer;

/**
 * @author emeroad
 */
public class SpanBo implements com.nhn.pinpoint.common.bo.Span {

    private static final int VERSION_SIZE = 1;
    // version 0 = prefix의 사이즈를 int로

    private byte version = 0;


//    private AgentKeyBo agentKeyBo;
    private String agentId;
    private String applicationId;
    private long agentStartTime;

    private String traceAgentId;
    private long traceAgentStartTime;
    private long traceTransactionSequence;
    private long spanId;
    private long parentSpanId;

    private long startTime;
    private int elapsed;

    private String rpc;
    private ServiceType serviceType;
    private String endPoint;
    private int apiId;

    private List<AnnotationBo> annotationBoList;
    private short flag; // optional
    private int errCode;

    private List<SpanEventBo> spanEventBoList;

    private long collectorAcceptTime;

    private boolean hasException = false;
    private int exceptionId;
    private String exceptionMessage;
    private String exceptionClass;

    
    private String remoteAddr; // optional

    public SpanBo(TSpan span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        this.agentId = span.getAgentId();
        this.applicationId = span.getApplicationName();
        this.agentStartTime = span.getAgentStartTime();

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(span.getTransactionId());
        this.traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            traceAgentId = this.agentId;
        }
        this.traceAgentStartTime = transactionId.getAgentStartTime();
        this.traceTransactionSequence = transactionId.getTransactionSequence();

        this.spanId = span.getSpanId();
        this.parentSpanId = span.getParentSpanId();

        this.startTime = span.getStartTime();
        this.elapsed = span.getElapsed();

        this.rpc = span.getRpc();

        this.serviceType = ServiceType.findServiceType(span.getServiceType());
        this.endPoint = span.getEndPoint();
        this.flag = span.getFlag();
        this.apiId = span.getApiId();

        this.errCode = span.getErr();
        
        this.remoteAddr = span.getRemoteAddr();
        

        // FIXME span.errCode는 span과 spanEvent의 에러를 모두 포함한 값.
        // exceptionInfo는 span자체의 에러정보이기 때문에 errCode가 0이 아니더라도 exceptionInfo는 null일 수 있음.
        final TIntStringValue exceptionInfo = span.getExceptionInfo();
        if (exceptionInfo != null) {
            this.hasException = true;
            this.exceptionId = exceptionInfo.getIntValue();
            this.exceptionMessage = exceptionInfo.getStringValue();
        }

        setAnnotationList(span.getAnnotations());
    }

    public SpanBo(String traceAgentId, long traceAgentStartTime, long traceTransactionSequence, long startTime, int elapsed, long spanId) {
        if (traceAgentId == null) {
            throw new NullPointerException("traceAgentId must not be null");
        }
        this.traceAgentId = traceAgentId;
        this.traceAgentStartTime = traceAgentStartTime;
        this.traceTransactionSequence = traceTransactionSequence;

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

	public String getTransactionId() {
        return TransactionIdUtils.formatString(traceAgentId, traceAgentStartTime, traceTransactionSequence);
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


    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
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

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    public List<AnnotationBo> getAnnotationBoList() {
        return annotationBoList;
    }

    public void setAnnotationList(List<TAnnotation> anoList) {
        if (anoList == null) {
            return;
        }
        List<AnnotationBo> boList = new ArrayList<AnnotationBo>(anoList.size());
        for (TAnnotation ano : anoList) {
            boList.add(new AnnotationBo(ano));
        }
        this.annotationBoList = boList;
    }

    public void setAnnotationBoList(List<AnnotationBo> anoList) {
        if (anoList == null) {
            return;
        }
        this.annotationBoList = anoList;
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
    
    public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
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
    	return -1L == parentSpanId;
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


    // io wirte시 variable encoding을 추가함.
    // 약 10%정도 byte 사이즈가 줄어드는 효과가 있음.
    public byte[] writeValue() {
        // var encoding 사용시 사이즈를 측정하기 어려움. 안되는것음 아님 편의상 그냥 자동 증가 buffer를 사용한다.
        // 향후 더 효율적으로 메모리를 사용하게 한다면 getBufferLength를 다시 부활 시키는것을 고려한다.
        final Buffer buffer = new AutomaticBuffer(256);

        buffer.put(version);

        // buffer.put(mostTraceID);
        // buffer.put(leastTraceID);

        buffer.putPrefixedString(agentId);
        // time의 경우도 현재 시간을 기준으로 var를 사용하는게 사이즈가 더 작음 6byte를 먹음.
        buffer.putVar(agentStartTime);

        // rowkey에 들어감.
        // buffer.put(spanID);
        buffer.put(parentSpanId);

        // 현재 시간이 기준이므로 var encoding
        buffer.putVar(startTime);
        buffer.putVar(elapsed);

        buffer.putPrefixedString(rpc);
        buffer.putPrefixedString(applicationId);
        buffer.put(serviceType.getCode());
        buffer.putPrefixedString(endPoint);
        buffer.putPrefixedString(remoteAddr);
        buffer.putSVar(apiId);

        // errCode code는 음수가 될수 있음.
        buffer.putSVar(errCode);

        if (hasException){
            buffer.put(true);
            buffer.putSVar(exceptionId);
            buffer.putPrefixedString(exceptionMessage);
        } else {
            buffer.put(false);
        }

        buffer.put(flag);

        return buffer.getBuffer();
    }

    public int readValue(byte[] bytes, int offset) {
        final Buffer buffer = new OffsetFixedBuffer(bytes, offset);

        this.version = buffer.readByte();

        // this.mostTraceID = buffer.readLong();
        // this.leastTraceID = buffer.readLong();

        this.agentId = buffer.readPrefixedString();
        this.agentStartTime = buffer.readVarLong();

        // this.spanID = buffer.readLong();
        this.parentSpanId = buffer.readLong();

        this.startTime = buffer.readVarLong();
        this.elapsed = buffer.readVarInt();

        this.rpc = buffer.readPrefixedString();
        this.applicationId = buffer.readPrefixedString();
        this.serviceType = ServiceType.findServiceType(buffer.readShort());
        this.endPoint = buffer.readPrefixedString();
        this.remoteAddr = buffer.readPrefixedString();
        this.apiId = buffer.readSVarInt();
        
        this.errCode = buffer.readSVarInt();

        this.hasException = buffer.readBoolean();
        if (hasException) {
            this.exceptionId = buffer.readSVarInt();
            this.exceptionMessage = buffer.readPrefixedString();
        }

        this.flag = buffer.readShort();

        return buffer.getOffset();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        sb.append("SpanBo{");
        sb.append("version=").append(version);
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", applicationId='").append(applicationId).append('\'');
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", traceAgentId='").append(traceAgentId).append('\'');
        sb.append(", traceAgentStartTime=").append(traceAgentStartTime);
        sb.append(", traceTransactionSequence=").append(traceTransactionSequence);
        sb.append(", spanId=").append(spanId);
        sb.append(", parentSpanId=").append(parentSpanId);
        sb.append(", startTime=").append(startTime);
        sb.append(", elapsed=").append(elapsed);
        sb.append(", rpc='").append(rpc).append('\'');
        sb.append(", serviceType=").append(serviceType);
        sb.append(", endPoint='").append(endPoint).append('\'');
        sb.append(", apiId=").append(apiId);
        sb.append(", annotationBoList=").append(annotationBoList);
        sb.append(", flag=").append(flag);
        sb.append(", errCode=").append(errCode);
        sb.append(", spanEventBoList=").append(spanEventBoList);
        sb.append(", collectorAcceptTime=").append(collectorAcceptTime);
        sb.append(", hasException=").append(hasException);
        sb.append(", exceptionId=").append(exceptionId);
        sb.append(", exceptionMessage='").append(exceptionMessage).append('\'');
        sb.append(", remoteAddr='").append(remoteAddr).append('\'');
        sb.append('}');
        return sb.toString();
    }
}