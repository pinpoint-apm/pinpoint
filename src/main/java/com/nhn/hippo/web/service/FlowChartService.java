package com.nhn.hippo.web.service;

import java.util.List;
import java.util.Set;

import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.filter.Filter;
import com.nhn.hippo.web.vo.Application;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.TraceId;

/**
 * @author netspider
 */
public interface FlowChartService {

	public Set<TraceId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to);

	public List<Application> selectAllApplicationNames();

	public ServerCallTree selectServerCallTree(TraceId traceId);

	public BusinessTransactions selectBusinessTransactions(Set<TraceId> traceIds, String applicationName, long from, long to, Filter filter);
	
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIdSet, Filter filter);
}
