package com.profiler.server.dao;

import org.apache.log4j.Logger;

public class Servers {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

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
