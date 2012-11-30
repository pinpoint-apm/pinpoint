package com.profiler.server.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServersDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public boolean upsert(String hostname, String agentId, long upTime) {
		try {

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	public void listAgentId(String hostname) {

	}
}
