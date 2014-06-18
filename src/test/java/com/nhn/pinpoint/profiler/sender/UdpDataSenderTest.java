package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinderInitializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author emeroad
 */
public class UdpDataSenderTest {
    @BeforeClass
    public static void before() {
        Slf4jLoggerBinderInitializer.beforeClass();
    }

    @AfterClass
    public static void after() {
        Slf4jLoggerBinderInitializer.afterClass();
    }



    @Test
    public void sendAndFlushChck() throws InterruptedException {
        UdpDataSender sender = new UdpDataSender("localhost", 9009, "test", 128, 1000, 1024*64*100);

        TAgentInfo agentInfo = new TAgentInfo();
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.stop();
}

}
