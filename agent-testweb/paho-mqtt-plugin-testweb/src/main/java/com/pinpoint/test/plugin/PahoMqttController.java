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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author Younsung Hwang
 */
@RestController
public class PahoMqttController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${mqtt.v3.broker.url}")
    private String v3BrokerUrl;
    @Value("${mqtt.v5.broker.url}")
    private String v5BrokerUrl;
    @Value("${mqtt.v3.topic}")
    private String v3Topic;
    @Value("${mqtt.v5.topic}")
    private String v5Topic;

    @GetMapping("/mqtt/v3/pub")
    public String mqttV3Pub(@RequestParam(defaultValue = "todareistodo") String payload) {
        try (org.eclipse.paho.client.mqttv3.MqttAsyncClient mqttClient =
                     new org.eclipse.paho.client.mqttv3.MqttAsyncClient(v3BrokerUrl, UUID.randomUUID().toString(), new org.eclipse.paho.client.mqttv3.persist.MemoryPersistence())) {
            mqttClient.connect().waitForCompletion(3000);
            mqttClient.publish(v3Topic, new org.eclipse.paho.client.mqttv3.MqttMessage(getBytes(payload))).waitForCompletion(3000);
            mqttClient.disconnect();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            logger.error("Failed to publish message.", e);
            return "Failed to publish message, cause : " + e.getMessage();
        }
        return "Successfully publish message[" + payload + "]";
    }

    @GetMapping("/mqtt/v5/pub")
    public String mqttV5Pub(@RequestParam(defaultValue = "todareistodo") String payload) {
        try {
            org.eclipse.paho.mqttv5.client.MqttAsyncClient mqttClient = new org.eclipse.paho.mqttv5.client.MqttAsyncClient(v5BrokerUrl, UUID.randomUUID().toString(), new org.eclipse.paho.mqttv5.client.persist.MemoryPersistence());
            mqttClient.connect().waitForCompletion(3000);
            mqttClient.publish(v5Topic, new org.eclipse.paho.mqttv5.common.MqttMessage(getBytes(payload))).waitForCompletion(3000);
            mqttClient.disconnect();
        } catch (org.eclipse.paho.mqttv5.common.MqttException e) {
            logger.error("Failed to publish message.", e);
            return "Failed to publish message, cause : " + e.getMessage();
        }
        return "Successfully publish message[" + payload + "]";
    }

    private byte[] getBytes(@RequestParam(defaultValue = "todareistodo") String payload) {
        return payload.getBytes(StandardCharsets.UTF_8);
    }
}
