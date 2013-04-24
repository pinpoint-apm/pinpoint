package com.nhn.hippo.web.dao;

import java.util.List;
import java.util.Map;

import com.nhn.hippo.web.vo.TerminalStatistics;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public interface TerminalStatisticsDao {
	/**
	 * 
	 * @param applicationName
	 * @param from
	 * @param to
	 * @return key=applicationname
	 */
	public List<Map<String, TerminalStatistics>> selectTerminal(String applicationName, long from, long to);
}
