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

package com.pinpoint.test.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.UUID;

/**
 * @author Younsung Hwang
 */
@Component
public class Subscriber {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private org.eclipse.paho.mqttv5.client.MqttAsyncClient v5Client;
    private org.eclipse.paho.client.mqttv3.MqttAsyncClient v3Client;

    @Autowired
    public Subscriber(@Value("${mqtt.v3.broker.url}") String v3BrokerUrl, @Value("${mqtt.v3.topic}") String v3Topic,
                      @Value("${mqtt.v5.broker.url}") String v5BrokerUrl, @Value("${mqtt.v5.topic}") String v5Topic)
            throws org.eclipse.paho.mqttv5.common.MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        logger.info("Init mqtt clients...");
        initV5Client(v3BrokerUrl, v3Topic);
        initV3Client(v5BrokerUrl, v5Topic);
    }

    private void initV5Client(String brokerUrl, String topic) throws org.eclipse.paho.mqttv5.common.MqttException {
        v5Client = new org.eclipse.paho.mqttv5.client.MqttAsyncClient(brokerUrl, UUID.randomUUID().toString(), new org.eclipse.paho.mqttv5.client.persist.MemoryPersistence());
        org.eclipse.paho.mqttv5.client.IMqttToken v5Token =  v5Client.connect();
        v5Token.waitForCompletion(3000);

        if(v5Token.isComplete()){
            initV5Subscribe(topic);
        } else {
            logger.error("Failed to connect mqtt v5 broker[{}]", brokerUrl);
        }
    }

    private void initV5Subscribe(String topic) throws org.eclipse.paho.mqttv5.common.MqttException {
        v5Client.subscribe(topic, 2).waitForCompletion(3000);
    }

    private void initV3Client(String brokerUrl, String topic) throws org.eclipse.paho.client.mqttv3.MqttException {
        v3Client = new org.eclipse.paho.client.mqttv3.MqttAsyncClient(brokerUrl, UUID.randomUUID().toString(), new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence());
        org.eclipse.paho.client.mqttv3.IMqttToken v3Token =  v3Client.connect();
        v3Token.waitForCompletion(3000);

        if(v3Token.isComplete()){
            initV3Subscribe(topic);
        } else {
            logger.error("Failed to connect mqtt v3 broker[{}]", brokerUrl);
        }
    }

    private void initV3Subscribe(String topic) throws org.eclipse.paho.client.mqttv3.MqttException {
        v3Client.subscribe(topic, 2).waitForCompletion(3000);
    }

    @PreDestroy
    public void preDestroy() throws org.eclipse.paho.mqttv5.common.MqttException, org.eclipse.paho.client.mqttv3.MqttException{
        logger.info("Disconnect mqtt clients...");
        v5Client.disconnect();
        v3Client.disconnect();
    }
}
