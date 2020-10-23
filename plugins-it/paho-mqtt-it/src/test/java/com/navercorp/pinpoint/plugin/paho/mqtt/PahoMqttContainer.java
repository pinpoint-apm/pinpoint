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

package com.navercorp.pinpoint.plugin.paho.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public abstract class PahoMqttContainer extends GenericContainer {

    private static final String IMAGE_NAME = "eclipse-mosquitto:1.6.12";
    private static final int DEFAULT_BROKER_PORT = 1883;
    private static final String LOCAL_BROKER_URL_PREFIX = "tcp://localhost:";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int brokerPort;

    public PahoMqttContainer() {
        this(DEFAULT_BROKER_PORT);
    }

    public PahoMqttContainer(int brokerPort) {
        super(IMAGE_NAME);
        this.brokerPort = brokerPort;
    }

    public int getBrokerPort() {
        return brokerPort;
    }

    public String getBrokerUrl() {
        Integer mappedPort = getMappedPort(brokerPort);
        String brokerUrl = LOCAL_BROKER_URL_PREFIX + mappedPort;
        return brokerUrl;
    }

    @Override
    protected void waitUntilContainerStarted() {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + TimeUnit.SECONDS.toMillis(30)) {
            try {
                Integer mappedPort = getMappedPort(brokerPort);
                if (checkBrokerStarted()) {
                    return;
                }
            } catch (Exception e) {
                logger.warn("Failed to check. message:{}", e.getMessage(), e);
            }
        }

        throw new ContainerLaunchException("Container startup failed");
    }

    abstract boolean checkBrokerStarted() throws Exception;

}