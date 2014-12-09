package com.navercorp.pinpoint.collector.cluster.zookeeper.job;

import com.navercorp.pinpoint.rpc.server.ChannelContext;

/**
 * @author koo.taejin
 */
public interface Job {

	
	ChannelContext getChannelContext();
	
	int getMaxRetryCount();
	
	int getCurrentRetryCount();
	
	void incrementCurrentRetryCount();
	
}
