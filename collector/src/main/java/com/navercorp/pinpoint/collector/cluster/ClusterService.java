package com.nhn.pinpoint.collector.cluster;


/**
 * @author koo.taejin <kr14910>
 */
public interface ClusterService {

	void setUp() throws Exception;
	
	void tearDown() throws Exception;
	
	boolean isEnable();
}
