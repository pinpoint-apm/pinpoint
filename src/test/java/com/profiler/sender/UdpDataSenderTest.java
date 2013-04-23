package com.profiler.sender;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.logging.LoggerBinder;
import com.profiler.logging.LoggerFactory;
import com.profiler.logging.Slf4jLoggerBinder;
import com.profiler.logging.Slf4jLoggerBinderInitializer;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.Before;
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
