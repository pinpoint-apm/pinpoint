package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationIndexDao {
	List<Application> selectAllApplicationNames();

	List<String> selectAgentIds(String applicationName);
	
	void deleteApplicationName(String applicationName);
	
	void deleteAgentId(String applicationName, String agentId);
}
