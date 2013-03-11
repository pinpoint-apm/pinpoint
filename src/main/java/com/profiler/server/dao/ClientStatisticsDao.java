package com.profiler.server.dao;

public interface ClientStatisticsDao {
	void update(String destApplicationName, short destServiceType, int elapsed, boolean isError);
}
