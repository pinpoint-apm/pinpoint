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

import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;

/**
 * Copy of https://github.com/chbatey/kafka-unit/blob/master/src/main/java/info/batey/kafka/unit/KafkaUnit.java
 * Some codes have been modified for testing from the copied code.
 */
public class ZookeeperUnitServer {
    private int port;
    private int maxConnections;
    private ServerCnxnFactory factory;

    public ZookeeperUnitServer(int port) {
        this.port = port;
        this.maxConnections = 16;
    }

    public ZookeeperUnitServer(int port, int maxConnections) {
        this.port = port;
        this.maxConnections = maxConnections;
    }

    public void startup() {
        final File snapshotDir;
        final File logDir;
        try {
            snapshotDir = Files.createTempDirectory("zookeeper-snapshot").toFile();
            logDir = Files.createTempDirectory("zookeeper-logs").toFile();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to start Kafka", ioe);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(snapshotDir);
                FileUtils.deleteDirectory(logDir);
            } catch (IOException ioe) {}
        }));

        try {
            int tickTime = 500;
            ZooKeeperServer zkServer = new ZooKeeperServer(snapshotDir, logDir, tickTime);
            factory = NIOServerCnxnFactory.createFactory();
            factory.configure(new InetSocketAddress("localhost", port), maxConnections);
            factory.startup(zkServer);
        } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
        } catch (IOException var6) {
            throw new RuntimeException("Unable to start ZooKeeper", var6);
        }

    }

    public void shutdown() {
        factory.shutdown();
    }
}
