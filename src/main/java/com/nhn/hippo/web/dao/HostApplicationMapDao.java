package com.nhn.hippo.web.dao;

import com.nhn.hippo.web.vo.Application;


/**
 * 
 * @author netspider
 * 
 */
public interface HostApplicationMapDao {
	public Application findApplicationName(String host, long from, long to);
}
