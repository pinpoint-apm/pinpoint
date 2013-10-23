package com.nhn.pinpoint.web.dao;

import java.util.List;
import java.util.Set;

import com.nhn.pinpoint.web.vo.ResultWithMark;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 *
 */
public interface ApplicationTraceIndexDao {
	ResultWithMark<List<TransactionId>, Long> scanTraceIndex(String applicationName, long start, long end, int limit);

//	List<List<List<TransactionId>>> multiScanTraceIndex(String[] applicationNames, long start, long end);

	List<List<Dot>> scanTraceScatter(String applicationName, long start, long end);

	List<Dot> scanTraceScatter2(String applicationName, long start, long end, int rowLimit);

	List<TransactionId> scanTraceScatterTransactionIdList(String applicationName, long start, long end, final int limit);
}
