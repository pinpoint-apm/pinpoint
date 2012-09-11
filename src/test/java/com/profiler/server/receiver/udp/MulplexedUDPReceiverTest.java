package com.profiler.server.receiver.udp;

import junit.framework.TestCase;

public class MulplexedUDPReceiverTest extends TestCase {

	public void testStart() throws Exception {
		DataReceiver receiver = new MulplexedUDPReceiver();
		receiver.start();
		Thread.sleep(1000 * 100);
		// receiver.shutdown(); z
	}

	public static void main(String[] args) {
		DataReceiver receiver = new MulplexedUDPReceiver();
		receiver.start();
	}
}
