package com.nhn.hippo.web.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nhn.hippo.web.calltree.RPCCallTree;
import com.nhn.hippo.web.calltree.ServerCallTree;
import com.nhn.hippo.web.vo.TraceId;
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
	public Set<TraceId> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to);

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
	public RPCCallTree selectRPCCallTree(Set<TraceId> traceIds);

	/**
	 * select call tree
	 * 
	 * @param traceIds
	 * @return
	 */
	public ServerCallTree selectServerCallTree(List<byte[]> traceIds);
}
