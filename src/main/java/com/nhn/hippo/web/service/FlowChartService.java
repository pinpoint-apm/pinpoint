package com.nhn.hippo.web.service;

import java.util.List;
import java.util.Set;

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.vo.TraceId;

/**
 * @author netspider
 */
public interface FlowChartService {

	/**
	 * select agentIds from application name
	 * 
	 * @param hosts
	 * @return
	 */
	public String[] selectAgentIdsFromApplicationName(String applicationName);

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
	 * select traceIds from ApplicationTraceIndex table
	 * 
	 * @param agentIds
	 * @param from
	 * @param to
	 * @return
	 */
	public Set<TraceId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to);

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
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds);

	/**
	 * select all application names
	 * 
	 * @return all of application names
	 */
	public List<String> selectAllApplicationNames();
	
	
    public String[] selectAgentIds(String[] hosts);

}
