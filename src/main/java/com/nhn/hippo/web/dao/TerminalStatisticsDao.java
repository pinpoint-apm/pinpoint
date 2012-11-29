package com.nhn.hippo.web.dao;

import java.util.List;

import com.nhn.hippo.web.vo.TerminalRequest;

/**
 * 
 * @author netspider
 * 
 */
public interface TerminalStatisticsDao {
	public List<List<TerminalRequest>> selectTerminal(String applicationName, long from, long to);
}
