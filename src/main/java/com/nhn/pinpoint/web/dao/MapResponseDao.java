package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author netspider
 * 
 */
public interface MapResponseDao {
	List<TransactionFlowStatistics> selectResponseTime(String applicationName, short applicationServiceType, long from, long to);

}
