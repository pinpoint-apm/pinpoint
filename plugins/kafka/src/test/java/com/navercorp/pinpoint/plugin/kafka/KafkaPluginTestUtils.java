/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public final class KafkaPluginTestUtils {

    public static String makeTopicString(int length) {
        return makeTopicString(length, true);
    }

    public static String makeTopicString(int length, boolean valid) {
        final Pattern pattern = Pattern.compile(ExcludeTopicFilter.TOPIC_PATTERN_VALUE);

        final List<Character> characterList = new ArrayList<>(length);
        if (!valid) {
            while (true) {
                final char c = (char) ThreadLocalRandom.current().nextInt(128);
                final Matcher matcher = pattern.matcher(String.valueOf(c));
                if (!matcher.matches()) {
                    characterList.add(c);
                    break;
                }
            }
        }

        while (characterList.size() < length) {
            final char c = (char) ThreadLocalRandom.current().nextInt(128);
            final Matcher matcher = pattern.matcher(String.valueOf(c));
            if (matcher.matches()) {
                characterList.add(c);
            }
        }

        Collections.shuffle(characterList);

        StringBuilder result = new StringBuilder(characterList.size());
        for (char eachChar : characterList) {
            result.append(eachChar);
        }
        return result.toString();
    }

    public static List<ConsumerRecord> createConsumerRecordList(String... topicNames) {
        List<ConsumerRecord> consumerRecordList = new ArrayList<>();
        for (String topicName : topicNames) {
            consumerRecordList.add(createConsumerRecord(topicName));
        }
        return consumerRecordList;
    }

    public static ConsumerRecord<String, String> createConsumerRecord(String topicName) {
        final int offset = ThreadLocalRandom.current().nextInt(1000);
        final String key = "hello_" + ThreadLocalRandom.current().nextInt(1000);
        final String value = "hello too_" + ThreadLocalRandom.current().nextInt(1000);
        return new ConsumerRecord<>(topicName, 0, offset, key, value);
    }
}
