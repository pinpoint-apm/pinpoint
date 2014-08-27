package com.nhn.pinpoint.collector.dao;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;

/**
 * @author emeroad
 */
public interface ApplicationIndexDao {
	public void insert(final TAgentInfo agentInfo);
}
