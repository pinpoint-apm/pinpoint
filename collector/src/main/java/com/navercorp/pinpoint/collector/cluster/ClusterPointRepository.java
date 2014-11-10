package com.nhn.pinpoint.collector.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterPointRepository<T extends ClusterPoint> implements ClusterPointLocator<T> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final CopyOnWriteArrayList<T> clusterPointRepository = new CopyOnWriteArrayList<T>();
	
	public boolean addClusterPoint(T clusterPoint) {
		boolean isAdd = clusterPointRepository.addIfAbsent(clusterPoint);
		
		if (!isAdd) {
			logger.warn("Already registered ClusterPoint({}).", clusterPoint);
		}
		
		return isAdd;
	}
	
	public boolean removeClusterPoint(T clusterPoint) {
		boolean isRemove = clusterPointRepository.remove(clusterPoint);
		
		if (!isRemove) {
			logger.warn("Already unregistered or not registered ClusterPoint({}).", clusterPoint);
		}
		
		return isRemove;
	}
	
	public List<T> getClusterPointList() {
		return new ArrayList<T>(clusterPointRepository);
	}
	
	public void clear() {
		
	}
	
}
