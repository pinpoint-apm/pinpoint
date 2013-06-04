package com.nhn.pinpoint.web.service;

import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.calltree.server.ServerCallTree;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.BusinessTransactions;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TraceId;

/**
 * @author netspider
 */
public interface FlowChartService {

	public Set<TraceId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to);

	public List<Application> selectAllApplicationNames();

	public ServerCallTree selectServerCallTree(TraceId traceId);

	public BusinessTransactions selectBusinessTransactions(Set<TraceId> traceIds, String applicationName, long from, long to, Filter filter);
	
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIdSet, Filter filter);
	
	public LinkStatistics linkStatistics(long from, long to, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType);

	public LinkStatistics linkStatisticsDetail(long from, long to, Set<TraceId> traceIdSet, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType, Filter filter);
}
