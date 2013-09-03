package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.thrift.dto.AgentInfo;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinderInitializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


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
        UdpDataSender sender = new UdpDataSender("localhost", 9009);

        AgentInfo agentInfo = new AgentInfo();
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
