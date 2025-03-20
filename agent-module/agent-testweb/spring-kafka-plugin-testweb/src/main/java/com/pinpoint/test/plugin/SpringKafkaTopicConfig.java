/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SpringKafkaTopicConfig {

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, SpringKafkaPluginTestConstants.BROKER_URL);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic1() {
        return new NewTopic(SpringKafkaPluginTestConstants.TOPIC_NAME, 1, (short) 1);
    }

    @Bean
    public NewTopic topic2() {
        return new NewTopic(SpringKafkaPluginTestConstants.PARTITIONED_TOPIC_NAME, 6, (short) 1);
    }

    @Bean
    public NewTopic topic3() {
        return new NewTopic(SpringKafkaPluginTestConstants.FILTERED_TOPIC_NAME, 1, (short) 1);
    }

    @Bean
    public NewTopic topic4() {
        return new NewTopic(SpringKafkaPluginTestConstants.GREETING_TOPIC_NAME, 1, (short) 1);
    }

    @Bean
    public NewTopic multiTypeTopic() {
        return new NewTopic(SpringKafkaPluginTestConstants.MULTI_TYPE_TOPIC_NAME, 1, (short) 1);
    }
}
