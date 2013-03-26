package com.nhn.hippo.web.dao;

import java.util.List;

import com.nhn.hippo.web.vo.Application;

/**
 * 
 * @author netspider
 * 
 */
public interface ApplicationIndexDao {
	public List<Application> selectAllApplicationNames();

	public String[] selectAgentIds(String applicationName);
}
