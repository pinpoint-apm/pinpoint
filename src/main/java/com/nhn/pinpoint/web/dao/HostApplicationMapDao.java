package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.vo.Application;


/**
 * 
 * @author netspider
 * 
 */
public interface HostApplicationMapDao {
	public Application findApplicationName(String host, long from, long to);
}
