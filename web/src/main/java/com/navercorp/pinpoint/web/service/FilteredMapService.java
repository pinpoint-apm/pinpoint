package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.vo.*;

import java.util.List;

/**
 * @author netspider
 * @author emeroad
 */
public interface FilteredMapService {

    LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit);

    LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, SelectedScatterArea area, int limit);

    LoadFactor linkStatistics(Range range, List<TransactionId> traceIdSet, Application sourceApplication, Application destinationApplication, Filter filter);

    ApplicationMap selectApplicationMap(List<TransactionId> traceIdList, Range originalRange, Range scanRange, Filter filter);

    ApplicationMap selectApplicationMap(TransactionId transactionId);
}
