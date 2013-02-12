package com.nhn.hippo.web.dao;

import java.util.List;

import com.nhn.hippo.web.vo.scatter.Dot;

/**
 *
 */
public interface ApplicationTraceIndexDao {
	List<List<byte[]>> scanTraceIndex(String applicationName, long start, long end);

	List<List<List<byte[]>>> multiScanTraceIndex(String[] applicationNames, long start, long end);

	List<List<Dot>> scanTraceScatter(String applicationName, long start, long end);
	
	List<Dot> scanTraceScatter2(String applicationName, long start, long end, int rowLimit);
}
