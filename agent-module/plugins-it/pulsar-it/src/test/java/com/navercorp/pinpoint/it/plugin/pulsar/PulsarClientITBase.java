/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.pulsar;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTrace;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.impl.ConsumerImpl;
import org.apache.pulsar.client.impl.ProducerImpl;
import org.apache.pulsar.client.impl.SendCallback;
import org.junit.jupiter.api.BeforeAll;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static test.pinpoint.plugin.pulsar.PulsarITConstants.MAX_TRACE_WAIT_TIME;
import static test.pinpoint.plugin.pulsar.PulsarITConstants.PULSAR_CLIENT_SERVICE_TYPE;
import static test.pinpoint.plugin.pulsar.PulsarITConstants.TOPIC;

/**
 * @author zhouzixin@apache.org
 */
public abstract class PulsarClientITBase {

    static String serviceUrl;

    @BeforeAll
    public static void beforeAll() {
        serviceUrl = System.getProperty("SERVICE_URL");
    }

    void verifySend() throws NoSuchMethodException {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.awaitTraceCount(2, 100, MAX_TRACE_WAIT_TIME);
        verifier.printCache();
        Method sendAsyncMethod = ProducerImpl.class.getDeclaredMethod("sendAsync", Message.class, SendCallback.class);
        ExpectedTrace.Builder eventBuilder = ExpectedTrace.createEventBuilder(PULSAR_CLIENT_SERVICE_TYPE);
        eventBuilder.setMethod(sendAsyncMethod);
        eventBuilder.setEndPoint(serviceUrl);
        eventBuilder.setDestinationId(serviceUrl);
        eventBuilder.setAnnotations(annotation("pulsar.topic", TOPIC));
        eventBuilder.setAnnotations(annotation("pulsar.broker.url", serviceUrl));
        ExpectedTrace producerTraces = eventBuilder.build();
        verifier.verifyDiscreteTrace(producerTraces);
        ExpectedTrace.Builder rootBuilder = ExpectedTrace.createRootBuilder(PULSAR_CLIENT_SERVICE_TYPE);
        rootBuilder.setMethodSignature("Pulsar Consumer Invocation");
        rootBuilder.setRpc("pulsar://topic=" + TOPIC + "&partition=-1");
        rootBuilder.setRemoteAddr(serviceUrl);
        rootBuilder.setAnnotations(annotation("pulsar.partition.index", -1));
        Method messageProcessed = ConsumerImpl.class.getDeclaredMethod("messageProcessed", Message.class);
        event(PULSAR_CLIENT_SERVICE_TYPE, messageProcessed);
        ExpectedTrace consumerTraces = rootBuilder.build();
        verifier.verifyDiscreteTrace(consumerTraces);
    }
}
