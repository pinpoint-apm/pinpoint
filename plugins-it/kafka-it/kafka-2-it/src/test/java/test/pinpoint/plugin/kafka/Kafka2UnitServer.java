/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import com.navercorp.pinpoint.testcase.util.SocketUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Copy of https://github.com/chbatey/kafka-unit/blob/master/src/main/java/info/batey/kafka/unit/KafkaUnit.java
 * Some codes have been modified for testing from the copied code.
 */
public class Kafka2UnitServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(Kafka2UnitServer.class);

    private final OffsetStore OFFSET_STORE = new OffsetStore();

    private Kafka2Server server;
    private TestConsumer TEST_CONSUMER;

    @Override
    public Properties beforeAll() {
        final int zkPort = SocketUtils.findAvailableTcpPort(10000, 19999);
        final int brokerPort = SocketUtils.findAvailableTcpPort(20000, 29999);
        server = new Kafka2Server(zkPort, brokerPort);

        server.startup();

        String brokerUrl = "localhost:" + brokerPort;
        TEST_CONSUMER = new TestConsumer(OFFSET_STORE, brokerUrl);
        TEST_CONSUMER.start();

        Properties properties = new Properties();
        properties.setProperty("PORT", String.valueOf(server.getBrokerPort()));
        properties.setProperty("OFFSET", String.valueOf(OFFSET_STORE.getOffset()));
        return properties;
    }

    @Override
    public void afterAll() {
        if (server != null) {
            server.shutdown();
        }
        try {
            TEST_CONSUMER.shutdown();
        } catch (InterruptedException e) {
        }
    }

    class Kafka2Server extends KafkaUnitServer {
        private KafkaServerStartable broker;

        public Kafka2Server(int zkPort, int brokerPort) {
            this(zkPort, brokerPort, 16);
        }

        public Kafka2Server(int zkPort, int brokerPort, int zkMaxConnections) {
            super(zkPort, brokerPort, zkMaxConnections);
        }

        @Override
        public void startup() {
            zookeeper = new ZookeeperUnitServer(zkPort, zkMaxConnections);
            zookeeper.startup();

            try {
                logDir = Files.createTempDirectory("kafka").toFile();
            } catch (IOException e) {
                throw new RuntimeException("Unable to start Kafka", e);
            }
            logDir.deleteOnExit();
            Runtime.getRuntime().addShutdownHook(new Thread(getDeleteLogDirectoryAction()));

            kafkaBrokerConfig.setProperty("zookeeper.connect", zookeeperString);
            kafkaBrokerConfig.setProperty("broker.id", "1");
            kafkaBrokerConfig.setProperty("host.name", "localhost");
            kafkaBrokerConfig.setProperty("port", Integer.toString(brokerPort));
            kafkaBrokerConfig.setProperty("log.dir", logDir.getAbsolutePath());
            kafkaBrokerConfig.setProperty("log.flush.interval.messages", String.valueOf(1));
            kafkaBrokerConfig.setProperty("delete.topic.enable", String.valueOf(true));
            kafkaBrokerConfig.setProperty("offsets.topic.replication.factor", String.valueOf(1));
            kafkaBrokerConfig.setProperty("auto.create.topics.enable", String.valueOf(true));
            broker = new KafkaServerStartable(new KafkaConfig(kafkaBrokerConfig));
            broker.startup();
        }

        @Override
        public void shutdown() {
            if (broker != null) {
                broker.shutdown();
                broker.awaitShutdown();
            }
            if (zookeeper != null) {
                zookeeper.shutdown();
            }
        }

        private Runnable getDeleteLogDirectoryAction() {
            return new Runnable() {
                @Override
                public void run() {
                    if (logDir != null) {
                        try {
                            FileUtils.deleteDirectory(logDir);
                        } catch (IOException e) {
                            logger.warn("Problems deleting temporary directory " + logDir.getAbsolutePath(), e);
                        }
                    }
                }
            };
        }
    }
}
