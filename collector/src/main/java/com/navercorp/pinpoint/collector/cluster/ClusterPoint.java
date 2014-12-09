package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.rpc.Future;

public interface ClusterPoint {

	void send(byte[] data);

	Future request(byte[] data);

}
