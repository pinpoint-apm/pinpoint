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

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.CommsCallback;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.UUID;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.root;

/**
 * @author Younsung Hwang
 * @author Taejin Koo
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-paho-mqtt-plugin")
@PinpointConfig("pinpoint-paho-mqttv3-plugin-test.config")
@Dependency({"org.eclipse.paho:org.eclipse.paho.client.mqttv3:[1.2.5][1.1.1][1.0.2]",
        "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5",
        "org.testcontainers:testcontainers:1.15.3"
})
public class PahoMqttV3ClientIT {

    private static PahoMqttContainer container = new PahoMqttV3Container();

    static String MESSAGE_PAYLOAD = "todareistodo";
    static String TOPIC = "pahotest";
    static String PAHO_MQTT_CLIENT = "PAHO_MQTT_CLIENT";
    static String PAHO_MQTT_CLIENT_INTERNAL = "PAHO_MQTT_CLIENT_INTERNAL";
    static int QOS = 2;
    static int WAIT_FOR_COMPLETION = 3000;

    static MqttAsyncClient mqttClient;

    @BeforeClass
    public static void before() throws MqttException {
        container.start();

        mqttClient = new MqttAsyncClient(container.getBrokerUrl(), UUID.randomUUID().toString(), new MemoryPersistence());
        IMqttToken token = mqttClient.connect();
        token.waitForCompletion(WAIT_FOR_COMPLETION);
        subscribe();
    }

    private static void subscribe() throws MqttException {
        IMqttToken mqttToken = mqttClient.subscribe(TOPIC, QOS);
        mqttToken.waitForCompletion(WAIT_FOR_COMPLETION);
    }

    @AfterClass
    public static void after() throws MqttException {
        mqttClient.disconnect();

        container.stop();
    }

    @Test
    public void methodTests() throws Exception {
        MqttMessage v3Message = new MqttMessage(MESSAGE_PAYLOAD.getBytes());
        v3Message.setQos(QOS);
        IMqttToken deliveryToken = mqttClient.publish(TOPIC, v3Message);
        deliveryToken.waitForCompletion(WAIT_FOR_COMPLETION);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(3, 100, 5000);
        verifier.verifyTraceCount(3);
        verifier.printCache();
        verifyTrace(verifier);
    }

    private void verifyTrace(PluginTestVerifier verifier) throws NoSuchMethodException {
        Method publishMethod = MqttAsyncClient.class.getDeclaredMethod("publish",
                String.class,
                MqttMessage.class,
                Object.class,
                org.eclipse.paho.client.mqttv3.IMqttActionListener.class
        );
        ExpectedTrace publishExpected = event(PAHO_MQTT_CLIENT, publishMethod, annotation("mqtt.broker.uri", container.getBrokerUrl()), annotation("mqtt.topic", TOPIC),
                annotation("mqtt.message.payload", MESSAGE_PAYLOAD), annotation("mqtt.qos", QOS));

        String expectedRpcName = "mqtt://topic=" + TOPIC + "&qos=" + QOS;
        ExpectedTrace messageArrivedInvocationExpected = root(PAHO_MQTT_CLIENT,
                "MQTT Message Arrived Invocation",
                expectedRpcName,
                null,
                container.getBrokerUrl(), annotation("mqtt.message.payload", MESSAGE_PAYLOAD));

        Method messageArrivedMethod = CommsCallback.class.getDeclaredMethod("messageArrived", MqttPublish.class);
        ExpectedTrace messageArrivedExpected = event(PAHO_MQTT_CLIENT_INTERNAL, messageArrivedMethod);

        verifier.verifyDiscreteTrace(publishExpected, messageArrivedInvocationExpected, messageArrivedExpected);
    }

    private static class PahoMqttV3Container extends PahoMqttContainer {

        @Override
        boolean checkBrokerStarted() throws Exception {

            MqttAsyncClient mqttClient = null;
            try {
                mqttClient = new MqttAsyncClient(getBrokerUrl(), UUID.randomUUID().toString(), new MemoryPersistence());
                IMqttToken token = mqttClient.connect();
                token.waitForCompletion(WAIT_FOR_COMPLETION);
                return true;
            } finally {
                if (mqttClient != null) {
                    mqttClient.disconnect();
                }
            }
        }

    }

}
