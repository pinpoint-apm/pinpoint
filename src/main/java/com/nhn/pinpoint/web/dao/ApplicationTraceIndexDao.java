package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.SelectedScatterArea;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 * @author emeroad
 * @author netspider
 */
public interface ApplicationTraceIndexDao {

	LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, Range range, int limit);

	LimitedScanResult<List<TransactionId>> scanTraceIndex(String applicationName, SelectedScatterArea range, int limit);

	List<Dot> scanTraceScatter(String applicationName, Range range, int limit);

	List<Dot> scanTraceScatter(String applicationName, SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed, int limit);
}
