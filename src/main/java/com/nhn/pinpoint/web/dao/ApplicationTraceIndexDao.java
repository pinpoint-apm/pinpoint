package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.web.vo.scatter.Dot;

/**
 *
 */
public interface ApplicationTraceIndexDao {
	List<List<TraceId>> scanTraceIndex(String applicationName, long start, long end);

	List<List<List<TraceId>>> multiScanTraceIndex(String[] applicationNames, long start, long end);

	List<List<Dot>> scanTraceScatter(String applicationName, long start, long end);

	List<Dot> scanTraceScatter2(String applicationName, long start, long end, int rowLimit);

	List<TraceId> scanTraceScatterTraceIdList(String applicationName, long start, long end, final int limit);
}
