package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.BusinessTransactions;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * @author netspider
 */
public interface FlowChartService {

	public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to, int limit);

	public List<Application> selectAllApplicationNames();

	public BusinessTransactions selectBusinessTransactions(List<TransactionId> traceIds, String applicationName, long from, long to, Filter filter);

	public LinkStatistics linkStatistics(long from, long to, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType);

	public LinkStatistics linkStatisticsDetail(long from, long to, List<TransactionId> traceIdSet, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType, Filter filter);

	public ApplicationMap selectApplicationMap(List<TransactionId> traceIdList, Filter filter);

	public ApplicationMap selectApplicationMap(TransactionId transactionId);
}
