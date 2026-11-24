/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.it.plugin.rocketmq;

import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycle;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import test.pinpoint.plugin.rocketmq.RocketMQITConstants;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * NameServer and Broker in one container.
 * <p>
 * A RocketMQ client asks the NameServer for the broker address and then connects to whatever the broker registered,
 * so a container-internal address would be unreachable from the test JVM. The broker is therefore told to register
 * the docker host address (brokerIP1) and to listen on a port that is published on the very same host port.
 */
public class TestBrokerServer implements SharedTestLifeCycle {

    private static final String IMAGE = "apache/rocketmq:5.3.2";
    private static final int NAMESRV_PORT = 9876;
    // the broker also opens "listenPort - 2" (the VIP channel that 4.x producers use by default)
    private static final int VIP_CHANNEL_PORT_OFFSET = 2;
    private static final long ROUTE_WAIT_TIME = 60000;

    private GenericContainer<?> container;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        final String host = toIpv4(DockerClientFactory.instance().dockerHostIpAddress());
        final int brokerPort = findBrokerPort();
        final int vipChannelPort = brokerPort - VIP_CHANNEL_PORT_OFFSET;
        final String brokerAddr = host + ":" + brokerPort;

        final String brokerConf = "/home/rocketmq/broker.conf";
        // a broker that boots before the NameServer accepts connections only registers 10 seconds later
        final String command = "printf 'brokerIP1=" + host + "\\nlistenPort=" + brokerPort
                + "\\nautoCreateTopicEnable=true\\n' > " + brokerConf
                + " && (sh mqnamesrv &)"
                + " && until (echo > /dev/tcp/localhost/" + NAMESRV_PORT + ") 2>/dev/null; do sleep 0.5; done"
                + " && exec sh mqbroker -n localhost:" + NAMESRV_PORT + " -c " + brokerConf;

        container = new GenericContainer<>(DockerImageName.parse(IMAGE));
        // the image sizes the heap from the host memory otherwise
        container.withEnv("JAVA_OPT_EXT", "-Xms512m -Xmx512m -Xmn256m");
        container.withCommand("bash", "-c", command);
        container.addExposedPort(NAMESRV_PORT);
        container.setPortBindings(Arrays.asList(
                brokerPort + ":" + brokerPort,
                vipChannelPort + ":" + vipChannelPort));
        container.waitingFor(Wait.forLogMessage(".*The broker\\[.*boot success.*\\n", 1)
                .withStartupTimeout(Duration.ofMinutes(3)));
        container.start();
        awaitBrokerRoute(brokerAddr);

        final String namesrvAddr = host + ":" + container.getMappedPort(NAMESRV_PORT);

        Properties properties = new Properties();
        properties.setProperty(RocketMQITConstants.NAMESRV_ADDR_PROPERTY, namesrvAddr);
        properties.setProperty(RocketMQITConstants.BROKER_ADDR_PROPERTY, brokerAddr);
        System.getProperties().putAll(properties);
        return properties;
    }

    @Override
    public void afterAll() {
        if (container != null) {
            container.stop();
        }
    }

    /**
     * "boot success" is logged before the NameServer necessarily knows the broker; clients need the route.
     */
    private void awaitBrokerRoute(String brokerAddr) {
        final long deadline = System.currentTimeMillis() + ROUTE_WAIT_TIME;
        String output = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                final Container.ExecResult result = container.execInContainer("sh", "mqadmin", "topicRoute",
                        "-n", "localhost:" + NAMESRV_PORT, "-t", RocketMQITConstants.AUTO_CREATE_TOPIC_KEY);
                output = result.getStdout() + result.getStderr();
                if (result.getStdout().contains(brokerAddr)) {
                    return;
                }
                Thread.sleep(500);
            } catch (IOException e) {
                throw new IllegalStateException("mqadmin failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("interrupted while waiting for the broker route", e);
            }
        }
        throw new IllegalStateException("broker " + brokerAddr + " not registered with the NameServer: " + output);
    }

    private static String toIpv4(String host) {
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address instanceof Inet4Address) {
                    return address.getHostAddress();
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalStateException("unresolvable docker host " + host, e);
        }
        throw new IllegalStateException("no IPv4 address for docker host " + host);
    }

    /**
     * @return a host port that is free together with its VIP channel port
     */
    private static int findBrokerPort() {
        for (int i = 0; i < 20; i++) {
            try (ServerSocket brokerSocket = new ServerSocket(0)) {
                final int brokerPort = brokerSocket.getLocalPort();
                try (ServerSocket vipChannelSocket = new ServerSocket(brokerPort - VIP_CHANNEL_PORT_OFFSET)) {
                    return brokerPort;
                }
            } catch (IOException ignored) {
                // one of the two ports is taken: try another pair
            }
        }
        throw new IllegalStateException("no free port pair for the RocketMQ broker");
    }
}
