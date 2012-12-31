package com.nhn.hippo.web.dao;

import java.util.List;

/**
 *
 */
public interface ApplicationTraceIndexDao {
	List<List<byte[]>> scanTraceIndex(String applicationName, long start, long end);

	List<List<List<byte[]>>> multiScanTraceIndex(String[] applicationNames, long start, long end);
}
