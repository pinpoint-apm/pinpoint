package com.nhn.pinpoint.collector.cluster;

import com.nhn.pinpoint.rpc.server.ChannelContext;

/**
 * @author koo.taejin
 */
public interface PinpointClusterManager {

	void register(ChannelContext channelContext);
	
	void unregister(ChannelContext channelContext);
	
}
