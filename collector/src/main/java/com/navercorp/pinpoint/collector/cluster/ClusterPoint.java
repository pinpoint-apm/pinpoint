package com.nhn.pinpoint.collector.cluster;

import com.nhn.pinpoint.rpc.Future;

public interface ClusterPoint {

	void send(byte[] data);

	Future request(byte[] data);

}
