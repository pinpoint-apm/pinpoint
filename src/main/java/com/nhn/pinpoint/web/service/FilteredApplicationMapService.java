package com.nhn.pinpoint.web.service;

import java.util.List;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.*;

/**
 * @author netspider
 * @author emeroad
 */
public interface FilteredApplicationMapService {

	public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit);

	public LoadFactor linkStatistics(Range range, List<TransactionId> traceIdSet, Application sourceApplication, Application destinationApplication, Filter filter);

	public ApplicationMap selectApplicationMap(List<TransactionId> traceIdList, Range range, Filter filter);

	public ApplicationMap selectApplicationMap(TransactionId transactionId);
}
