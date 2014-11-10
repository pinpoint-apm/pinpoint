package com.nhn.pinpoint.collector.cluster;

public interface TargetClusterPoint extends ClusterPoint {

	String getApplicationName();

	String getAgentId();

	long getStartTimeStamp();

	String gerVersion();
	
}
