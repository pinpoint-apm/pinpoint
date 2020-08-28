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
import com.navercorp.pinpoint.test.plugin.*;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.internal.CommsCallback;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttPublish;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.UUID;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

/**
 * @author Younsung Hwang
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-paho-mqtt-plugin")
@PinpointConfig("pinpoint-paho-mqttv5-plugin-test.config")
@Dependency({"org.eclipse.paho:org.eclipse.paho.mqttv5.client:[1.2.5]"})
public class PahoMqttV5ClientIT {

    static String MESSAGE_PAYLOAD = "todareistodo";
    static String TOPIC = "pahotest";
    static String PAHO_MQTT_CLIENT = "PAHO_MQTT_CLIENT";
    static String PAHO_MQTT_CLIENT_INTERNAL = "PAHO_MQTT_CLIENT_INTERNAL";
    static String BROKER_URL = "tcp://localhost:1883";
    static int QOS = 2;
    static int WAIT_FOR_COMPLETION = 3000;

    static MqttAsyncClient mqttClient;

    @BeforeClass
    public static void before() throws MqttException {
        mqttClient = new MqttAsyncClient(BROKER_URL, UUID.randomUUID().toString(), new MemoryPersistence());
        IMqttToken token =  mqttClient.connect();
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
    }

    @Test
    public void methodTests() throws Exception {
        MqttMessage v5Message = new MqttMessage(MESSAGE_PAYLOAD.getBytes());
        v5Message.setQos(QOS);
        IMqttToken deliveryToken = mqttClient.publish(TOPIC, v5Message);
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
                org.eclipse.paho.mqttv5.client.MqttActionListener.class
        );
        ExpectedTrace publishExpected = event(PAHO_MQTT_CLIENT, publishMethod, annotation("mqtt.broker.uri", BROKER_URL), annotation("mqtt.topic", TOPIC),
                annotation("mqtt.message.payload", MESSAGE_PAYLOAD), annotation("mqtt.qos", QOS));

        String expectedRpcName = "mqtt://topic="+TOPIC+"&qos="+QOS;
        ExpectedTrace messageArrivedInvocationExpected = root(PAHO_MQTT_CLIENT,
                "MQTT Message Arrived Invocation",
                expectedRpcName,
                null,
                BROKER_URL, annotation("mqtt.message.payload", MESSAGE_PAYLOAD));

        Method messageArrivedMethod = CommsCallback.class.getDeclaredMethod("messageArrived", MqttPublish.class);
        ExpectedTrace messageArrivedExpected = event(PAHO_MQTT_CLIENT_INTERNAL, messageArrivedMethod);

        verifier.verifyDiscreteTrace(publishExpected, messageArrivedInvocationExpected, messageArrivedExpected);
    }

}

