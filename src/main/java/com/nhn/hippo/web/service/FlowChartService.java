package com.nhn.hippo.web.service;

import java.util.List;
import java.util.Map;

import com.nhn.hippo.web.calltree.RPCCallTree;
import com.profiler.common.dto.thrift.Span;

/**
 * 
 * @author netspider
 * 
 */
public interface FlowChartService {

	/**
	 * select agentIds from Server table
	 * 
	 * @param hosts
	 * @return
	 */
	public String[] selectAgentIds(String[] hosts);

	/**
	 * select traceIds from TraceIndex table
	 * 
	 * @param agentIds
	 * @param from
	 * @param to
	 * @return
	 */
	public List<byte[]> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to);

	/**
	 * select Traces from Trace table
	 * 
	 * @param traceIds
	 * @return
	 */
	public Map<byte[], List<Span>> selectTraces(List<byte[]> traceIds);

	/**
	 * select call tree
	 * 
	 * @param traceIds
	 * @return
	 */
	public RPCCallTree selectCallTree(List<byte[]> traceIds);
}
