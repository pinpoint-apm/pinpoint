package com.nhn.hippo.web.dao;

import java.util.List;
import java.util.Map;

import com.nhn.hippo.web.vo.ClientStatistics;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface ClientStatisticsDao {
	public List<Map<String, ClientStatistics>> selectClient(String applicationName, short serviceType, long from, long to);
}