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
package test.pinpoint.plugin.pulsar;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;

import java.io.Closeable;
import java.io.IOException;

import static test.pinpoint.plugin.pulsar.PulsarITConstants.SUB_NAME;
import static test.pinpoint.plugin.pulsar.PulsarITConstants.TOPIC;

/**
 * @author zhouzixin@apache.org
 */
public class TestConsumer implements Closeable {

    private final String serviceUrl;
    private Consumer<byte[]> consumer;

    public TestConsumer(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void start() {
        PulsarClient client;
        try {
            client = PulsarClient.builder()
                    .serviceUrl(serviceUrl)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create PulsarClient.", e);
        }

        try {
            consumer = client.newConsumer()
                    .topic(TOPIC)
                    .subscriptionName(SUB_NAME)
                    .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                    .subscribe();
        } catch (Exception e) {
            try {
                client.close();
            } catch (Exception ignored) {
            }
            throw new RuntimeException("Failed to create Consumer.", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (consumer != null) {
            consumer.close();
        }
    }
}
