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

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.io.Closeable;
import java.io.IOException;

import static test.pinpoint.plugin.pulsar.PulsarITConstants.MESSAGE;
import static test.pinpoint.plugin.pulsar.PulsarITConstants.TOPIC;

/**
 * @author zhouzixin@apache.org
 */
public class TestProducer implements Closeable {

    private Producer<byte[]> producer;

    public void sendMessage(String serviceUrl) {
        PulsarClient client;
        try {
            client = PulsarClient.builder().serviceUrl(serviceUrl).build();
        } catch (PulsarClientException e) {
            throw new RuntimeException("Failed to create PulsarClient.", e);
        }

        try {
            producer = client.newProducer()
                    .topic(TOPIC)
                    .create();
            producer.newMessage()
                    .value(MESSAGE.getBytes())
                    .send();
        } catch (PulsarClientException e) {
            try {
                client.close();
            } catch (PulsarClientException ignored) {
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (producer != null) {
            producer.close();
        }
    }
}
