package com.nhn.hippo.web.dao;

import java.util.List;

import com.nhn.hippo.web.vo.TerminalStatistics;

/**
 * 
 * @author netspider
 * 
 */
public interface TerminalStatisticsDao {
	public List<List<TerminalStatistics>> selectTerminal(String applicationName, long from, long to);
}
