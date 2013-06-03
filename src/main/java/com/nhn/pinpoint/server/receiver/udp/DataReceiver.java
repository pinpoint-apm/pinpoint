package com.nhn.pinpoint.server.receiver.udp;

import java.util.concurrent.Future;

public interface DataReceiver {
	Future<Boolean> start();

	void shutdown();
}
