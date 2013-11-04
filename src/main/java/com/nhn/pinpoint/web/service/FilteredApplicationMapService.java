package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * @author netspider
 */
public interface FilteredApplicationMapService {

	public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to, int limit);

	public LinkStatistics linkStatistics(long from, long to, List<TransactionId> traceIdSet, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType, Filter filter);

	public ApplicationMap selectApplicationMap(List<TransactionId> traceIdList, Filter filter);

	public ApplicationMap selectApplicationMap(TransactionId transactionId);
}
