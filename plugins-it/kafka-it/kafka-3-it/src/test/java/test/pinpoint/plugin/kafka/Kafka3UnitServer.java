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

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import org.apache.kafka.common.utils.Time;
import scala.Option;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Copy of https://github.com/chbatey/kafka-unit/blob/master/src/main/java/info/batey/kafka/unit/KafkaUnit.java
 * Some codes have been modified for testing from the copied code.
 */
public class Kafka3UnitServer extends KafkaUnitServer {

    private static final Logger logger = LogManager.getLogger(Kafka3UnitServer.class);
    private KafkaServer broker;

    public Kafka3UnitServer(int zkPort, int brokerPort) {
        this(zkPort, brokerPort, 16);
    }

    public Kafka3UnitServer(int zkPort, int brokerPort, int zkMaxConnections) {
        super(zkPort, brokerPort, zkMaxConnections);
    }

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
        broker = new KafkaServer(new KafkaConfig(kafkaBrokerConfig), Time.SYSTEM, Option.apply(null), false);
        broker.startup();
    }

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
