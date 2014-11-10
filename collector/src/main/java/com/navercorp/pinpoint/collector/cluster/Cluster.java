package com.nhn.pinpoint.collector.cluster;

import java.net.InetSocketAddress;

public interface Cluster {

	void connectPointIfAbsent(InetSocketAddress address);

	void disconnectPoint(InetSocketAddress address);
	
}
