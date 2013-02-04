package com.profiler.common.bo;

import java.util.ArrayList;
import java.util.List;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.Annotation;
import com.profiler.common.dto.thrift.SubSpan;
import com.profiler.common.util.Buffer;
import com.profiler.common.util.BytesUtils;

/**
 *
 */
public class SubSpanBo implements Span {
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

	private String agentId;
	private long mostTraceId;
	private long leastTraceId;

	private int spanId;
	private short sequence;

	private int startElapsed;
	private int endElapsed;

	private String rpc;
	private String serviceName;
	private ServiceType serviceType;
	private String endPoint;
	private List<AnnotationBo> annotationBoList;

	private boolean exception = false;
	
	private int depth = -1;
	private int nextSpanId = -1;

	public SubSpanBo() {
	}

	public SubSpanBo(com.profiler.common.dto.thrift.Span tSpan, SubSpan tSubSpan) {
		this.agentId = tSpan.getAgentId();

		this.mostTraceId = tSpan.getMostTraceId();
		this.leastTraceId = tSpan.getLeastTraceId();

		this.spanId = tSpan.getSpanId();
		this.sequence = tSubSpan.getSequence();

		this.startElapsed = tSubSpan.getStartElapsed();
		this.endElapsed = tSubSpan.getEndElapsed();

		this.rpc = tSubSpan.getRpc();
		this.serviceName = tSubSpan.getServiceName();
		this.serviceType = ServiceType.parse(tSubSpan.getServiceType());

		this.endPoint = tSubSpan.getEndPoint();
		
        this.exception = tSubSpan.isErr();
		
		if (tSubSpan.isSetDepth()) {
			this.depth = tSubSpan.getDepth();
		}
        
		if (tSubSpan.isSetNextSpanId()) {
			this.nextSpanId = tSubSpan.getNextSpanId();
		}
        
		setAnnotationBoList(tSubSpan.getAnnotations());
	}

	public SubSpanBo(com.profiler.common.dto.thrift.SubSpanList subSpanList, SubSpan subSpan) {
		this.agentId = subSpanList.getAgentId();

		this.mostTraceId = subSpanList.getMostTraceId();
		this.leastTraceId = subSpanList.getLeastTraceId();

		this.spanId = subSpanList.getSpanId();
		this.sequence = subSpan.getSequence();

		this.startElapsed = subSpan.getStartElapsed();
		this.endElapsed = subSpan.getEndElapsed();

		this.rpc = subSpan.getRpc();
		this.serviceName = subSpan.getServiceName();
		this.serviceType = ServiceType.parse(subSpan.getServiceType());

		this.endPoint = subSpan.getEndPoint();
		
		if (subSpan.isSetDepth()) {
			this.depth = subSpan.getDepth();
		}

		if (subSpan.isSetNextSpanId()) {
			this.nextSpanId = subSpan.getNextSpanId();
		}
		
		setAnnotationBoList(subSpan.getAnnotations());
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
		
		if (subSpan.isSetDepth()) {
			this.depth = subSpan.getDepth();
		}

		if (subSpan.isSetNextSpanId()) {
			this.nextSpanId = subSpan.getNextSpanId();
		}
		
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

	public void setSpanId(int spanId) {
		this.spanId = spanId;
	}

	public int getSpanId() {
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

	public int getNextSpanId() {
		return nextSpanId;
	}

	public void setNextSpanId(int nextSpanId) {
		this.nextSpanId = nextSpanId;
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
		size += 1 + 1 + 1 + 1 + 1+ VERSION_SIZE; // chunk size chunk
		// size = size + TIMESTAMP + MOSTTRACEID + LEASTTRACEID + SPANID +
		// PARENTSPANID + FLAG + TERMINAL;
		size += SERVICETYPE;

		// startTime 4, elapsed 4, depth 4, nextSpanId 4
		size += 16;
		return size;
	}

	public byte[] writeValue() {
		byte[] agentIDBytes = BytesUtils.getBytes(agentId);
		byte[] rpcBytes = BytesUtils.getBytes(rpc);
		byte[] serviceNameBytes = BytesUtils.getBytes(serviceName);
		byte[] endPointBytes = BytesUtils.getBytes(endPoint);

		int bufferLength = getBufferLength(agentIDBytes.length, rpcBytes.length, serviceNameBytes.length, endPointBytes.length);
		int annotationSize = getAnnotationBufferSize(annotationBoList);

		Buffer buffer = new Buffer(bufferLength + annotationSize);

		buffer.put(version);

		// buffer.put(mostTraceID);
		// buffer.put(leastTraceID);

		buffer.put1PrefixedBytes(agentIDBytes);

		buffer.put(startElapsed);
		buffer.put(endElapsed);
		// Qualifier에서 읽어서 set하므로 필요 없음.
		// buffer.put(sequence);

		buffer.put1PrefixedBytes(rpcBytes);
		buffer.put1PrefixedBytes(serviceNameBytes);
		buffer.put(serviceType.getCode());
		buffer.put1PrefixedBytes(endPointBytes);

		buffer.put(exception);
		
		buffer.put(depth);
		buffer.put(nextSpanId);
		
		writeAnnotation(buffer);

		return buffer.getBuffer();
	}

	private void writeAnnotation(Buffer buffer) {
		buffer.put(annotationBoList.size());
		for (AnnotationBo annotation : annotationBoList) {
			annotation.writeValue(buffer);
		}
	}

	private int getAnnotationBufferSize(List<AnnotationBo> boList) {
		int size = 0;
		for (AnnotationBo ano : boList) {
			size += ano.getBufferSize();
		}
		// size
		size += 4;
		return size;
	}

	public int readValue(byte[] bytes, int offset) {
		Buffer buffer = new Buffer(bytes, offset);

		this.version = buffer.readByte();

		// this.mostTraceID = buffer.readLong();
		// this.leastTraceID = buffer.readLong();

		this.agentId = buffer.read1PrefixedString();

		this.startElapsed = buffer.readInt();
		this.endElapsed = buffer.readInt();
		// Qualifier에서 읽어서 가져오므로 하지 않아도 됨.
		// this.sequence = buffer.readShort();

		this.rpc = buffer.read1UnsignedPrefixedString();
		this.serviceName = buffer.read1UnsignedPrefixedString();
		this.serviceType = ServiceType.parse(buffer.readShort());
		this.endPoint = buffer.read1UnsignedPrefixedString();

		this.exception = buffer.readBoolean();
		
		this.depth = buffer.readInt();
		this.nextSpanId = buffer.readInt();
		
		this.annotationBoList = readAnnotation(buffer);
		return buffer.getOffset();
	}

	private List<AnnotationBo> readAnnotation(Buffer buffer) {
		int count = buffer.readInt();
		List<AnnotationBo> list = new ArrayList<AnnotationBo>();
		for (int i = 0; i < count; i++) {
			AnnotationBo bo = new AnnotationBo();
			bo.readValue(buffer);
			list.add(bo);
		}
		return list;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.getClass().getName()).append("={");
		sb.append("\n\tversion=").append(version).append(", agentId=").append(agentId);
		sb.append("\n\tmostTraceId=").append(mostTraceId).append(", leastTraceId=").append(leastTraceId);
		sb.append("\n\tspanId=").append(spanId).append(", sequence=").append(sequence);
		sb.append("\n\tstartElapsed=").append(startElapsed).append(", endElapsed=").append(endElapsed);
		sb.append("\n\trpc=").append(rpc).append(", serviceName=").append(serviceName).append(", endPoint=").append(endPoint);
		sb.append("\n\texception=").append(exception);
		sb.append("\n\tdepth=").append(depth);
		sb.append("\n\tnextSpanId=").append(nextSpanId);
		sb.append("\n\tannotations={");
		for (AnnotationBo a : annotationBoList) {
			sb.append("\n\t\tkey=").append(a.getKey());
			sb.append(", value=").append(a.getValue());
		}
		sb.append("\n\t}");
		sb.append("}");

		return sb.toString();
	}
}
