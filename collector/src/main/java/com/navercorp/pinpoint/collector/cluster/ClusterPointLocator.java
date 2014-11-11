package com.nhn.pinpoint.collector.cluster;

import java.util.List;

public interface ClusterPointLocator<T extends ClusterPoint> {

	List<T> getClusterPointList();
	
}
