package com.nhn.hippo.web.dao;

import java.util.List;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationIndexDao {
	public List<String> selectAllApplicationNames();

	public String[] selectAgentIds(String applicationName);
}
