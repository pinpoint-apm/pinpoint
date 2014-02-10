package com.nhn.pinpoint.web.dao;

import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;


/**
 * 
 * @author netspider
 * 
 */
public interface HostApplicationMapDao {
	public Application findApplicationName(String host, Range range);
}
