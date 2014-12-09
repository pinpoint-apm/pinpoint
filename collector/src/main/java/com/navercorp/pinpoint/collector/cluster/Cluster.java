package com.navercorp.pinpoint.collector.cluster;

import java.net.InetSocketAddress;

public interface Cluster {

	void connectPointIfAbsent(InetSocketAddress address);

	void disconnectPoint(InetSocketAddress address);
	
}
