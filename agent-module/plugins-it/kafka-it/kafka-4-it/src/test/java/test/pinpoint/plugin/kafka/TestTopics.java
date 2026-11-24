/*
 * Copyright 2026 NAVER Corp.
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

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class TestTopics {

    private TestTopics() {
    }

    public static void create(String brokerUrl, String... topics) {
        List<NewTopic> toCreate = new ArrayList<>();
        for (String topic : topics) {
            toCreate.add(new NewTopic(topic, 1, (short) 1));
        }

        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);

        try (AdminClient adminClient = AdminClient.create(config)) {
            adminClient.createTopics(toCreate).all().get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create topics " + toCreate, e);
        }
    }
}
