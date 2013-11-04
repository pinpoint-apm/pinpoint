package com.nhn.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.util.TransactionIdUtils;

/**
 * @author emeroad
 */
public class BusinessTransaction {
	private final List<Trace> traces = new ArrayList<Trace>();
	private final String rpc;

	private int calls = 0;
	private int error = 0;
	private long totalTime = 0;
	private long maxTime = 0;
	private long minTime = 0;

	public BusinessTransaction(SpanBo span) {
		this.rpc = span.getRpc();

		long elapsed = span.getElapsed();
		totalTime = maxTime = minTime = elapsed;

        String traceIdString = TransactionIdUtils.formatString(span.getTraceAgentId(), span.getTraceAgentStartTime(), span.getTraceTransactionSequence());
        Trace trace = new Trace(traceIdString, elapsed, span.getCollectorAcceptTime(), span.getErrCode());
        this.traces.add(trace);
		calls++;
		if(span.getErrCode() > 0) {
			error++;
		}
	}

	public void add(SpanBo span) {
		long elapsed = span.getElapsed();

		totalTime += elapsed;
		if (maxTime < elapsed) {
			maxTime = elapsed;
        }
		if (minTime > elapsed) {
			minTime = elapsed;
        }

        String traceIdString = TransactionIdUtils.formatString(span.getTraceAgentId(), span.getTraceAgentStartTime(), span.getTraceTransactionSequence());
        Trace trace = new Trace(traceIdString, elapsed, span.getCollectorAcceptTime(), span.getErrCode());
		this.traces.add(trace);

		if(span.getErrCode() > 0) {
			error++;
		}
		
		//if (span.getParentSpanId() == -1) {
			calls++;
		//}
	}

	public String getRpc() {
		return rpc;
	}

	public List<Trace> getTraces() {
		return traces;
	}

	public int getCalls() {
		return calls;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public long getMinTime() {
		return minTime;
	}
	
	public int getError() {
		return error;
	}
}
