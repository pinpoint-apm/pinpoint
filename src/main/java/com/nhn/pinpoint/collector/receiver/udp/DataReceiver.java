package com.nhn.pinpoint.collector.receiver.udp;

import java.util.concurrent.Future;

public interface DataReceiver {
	Future<Boolean> start();

	void shutdown();
}
