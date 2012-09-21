package com.profiler.server.receiver.udp;

import com.profiler.server.spring.ApplicationContextUtils;
import junit.framework.TestCase;
import org.springframework.context.support.GenericApplicationContext;

public class MulplexedUDPReceiverTest extends TestCase {

	public void testStart() throws Exception {
        GenericApplicationContext context = ApplicationContextUtils.createContext();
        DataReceiver receiver = new MulplexedUDPReceiver(context);
		receiver.start();
		Thread.sleep(1000 * 100);
		// receiver.shutdown(); z
        receiver.shutdown();
        context.close();
	}

	public static void main(String[] args) {
        GenericApplicationContext context = ApplicationContextUtils.createContext();
		DataReceiver receiver = new MulplexedUDPReceiver(context);
		receiver.start();
	}
}
