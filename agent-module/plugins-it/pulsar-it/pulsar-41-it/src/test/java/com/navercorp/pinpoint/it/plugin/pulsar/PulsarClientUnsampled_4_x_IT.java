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
package com.navercorp.pinpoint.it.plugin.pulsar;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.api.PluginForkedTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Sampling is disabled: the trace of the test method is not sampled, so the producer adds Pinpoint-Sampled=s0 only.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-pulsar-client-unsampled.config")
@Dependency({"org.apache.pulsar:pulsar-client:[4.1.0,4.max)"})
@SharedDependency({
        TestcontainersOption.TEST_CONTAINER,
        TestcontainersOption.PULSAR,
        "org.apache.pulsar:pulsar-client:4.1.0",
})
@SharedTestLifeCycleClass(TestBrokerServer.class)
public class PulsarClientUnsampled_4_x_IT {

    private static final long MAX_WAIT_TIME = 10000;

    private static String serviceUrl;

    @BeforeAll
    public static void beforeAll() {
        serviceUrl = System.getProperty("SERVICE_URL");
    }

    @Test
    public void sendUnsampled() throws Exception {
        final String topic = "persistent://public/default/pulsar-it-" + UUID.randomUUID();
        final byte[] payload = "sendUnsampled".getBytes(StandardCharsets.UTF_8);

        try (PulsarClient client = PulsarClient.builder().serviceUrl(serviceUrl).build();
             Consumer<byte[]> consumer = client.newConsumer()
                     .topic(topic)
                     .subscriptionName("pulsar-it-subscription")
                     .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                     .subscribe();
             Producer<byte[]> producer = client.newProducer().topic(topic).create()) {

            producer.newMessage().value(payload).send();

            // receive() on a thread without a trace, like a real consumer
            final CompletableFuture<Message<byte[]>> future = new CompletableFuture<>();
            final Thread receiver = new Thread(() -> {
                try {
                    future.complete(consumer.receive((int) MAX_WAIT_TIME, TimeUnit.MILLISECONDS));
                } catch (Throwable th) {
                    future.completeExceptionally(th);
                }
            }, "pulsar-it-receiver");
            receiver.setDaemon(true);
            receiver.start();

            final Message<byte[]> message = future.get(MAX_WAIT_TIME * 2, TimeUnit.MILLISECONDS);
            Assertions.assertNotNull(message, "message not received");
            consumer.acknowledge(message);

            Assertions.assertEquals("s0", message.getProperty("Pinpoint-Sampled"), "properties=" + message.getProperties());
            Assertions.assertNull(message.getProperty("Pinpoint-TraceID"), "properties=" + message.getProperties());
            Assertions.assertNull(message.getProperty("Pinpoint-SpanID"), "properties=" + message.getProperties());

            final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();
            verifier.verifyTraceCount(0);
        }
    }
}
