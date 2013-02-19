package com.nhn.hippo.web.service;

import java.util.List;
import java.util.Set;

import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.scatter.Dot;

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
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds);

	/**
	 * 
	 * @param traceIds
	 * @param applicationName
	 * @param from
	 * @param to
	 * @return
	 */
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds, String applicationName, long from, long to);

	/**
	 * select all application names
	 * 
	 * @return all of application names
	 */
	public List<String> selectAllApplicationNames();

	public String[] selectAgentIds(String[] hosts);
	
	public ServerCallTree selectServerCallTree(TraceId traceId);
	
	public List<Dot> selectScatterData(String applicationName, long from, long to);
	
	public List<Dot> selectScatterData(String applicationName, long from, long to, int limit);
	
	public BusinessTransactions selectBusinessTransactions(Set<TraceId> traceIds, String applicationName, long from, long to);
}
