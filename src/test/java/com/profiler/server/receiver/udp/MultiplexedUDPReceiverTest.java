package com.profiler.server.receiver.udp;

import com.profiler.server.spring.ApplicationContextUtils;
import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

public class MultiplexedUDPReceiverTest {

    @Test
	public void startStop() throws Exception {
        GenericApplicationContext context = ApplicationContextUtils.createContext();
        DataReceiver receiver = new MultiplexedUDPReceiver(context);
		receiver.start();

		Thread.sleep(1000 * 2);

        receiver.shutdown();
        context.close();

	}


}
