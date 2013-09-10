package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 *
 */
public interface ApplicationTraceIndexDao {
	List<List<TransactionId>> scanTraceIndex(String applicationName, long start, long end);

//	List<List<List<TransactionId>>> multiScanTraceIndex(String[] applicationNames, long start, long end);

	List<List<Dot>> scanTraceScatter(String applicationName, long start, long end);

	List<Dot> scanTraceScatter2(String applicationName, long start, long end, int rowLimit);

	List<TransactionId> scanTraceScatterTransactionIdList(String applicationName, long start, long end, final int limit);
}
