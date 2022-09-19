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

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExcludeTopicFilterTest {

    @Test
    public void topicSizeTest() {
        assertFilter(KafkaPluginTestUtils.makeTopicString(ExcludeTopicFilter.MAX_LENGTH), true);
        assertFilter(KafkaPluginTestUtils.makeTopicString(ExcludeTopicFilter.MAX_LENGTH + 1), false);
    }

    @Test
    public void topicPatternTest() {
        int topicNameLength = ThreadLocalRandom.current().nextInt(1, ExcludeTopicFilter.MAX_LENGTH);
        assertFilter(KafkaPluginTestUtils.makeTopicString(topicNameLength, true), true);
        assertFilter(KafkaPluginTestUtils.makeTopicString(topicNameLength, false), false);

        final String random1 = KafkaPluginTestUtils.makeTopicString(topicNameLength);
        final String random2 = KafkaPluginTestUtils.makeTopicString(topicNameLength);
        assertFilter(random1 + "," + random2, random1, true);
        assertFilter(random1 + "," + random2, random2, true);
    }

    private void assertFilter(String topicValue, boolean expected) {
        assertFilter(topicValue, topicValue, expected);
    }

    private void assertFilter(String filterValue, String topicValue, boolean expected) {
        final ExcludeTopicFilter filter = new ExcludeTopicFilter(filterValue);
        Assertions.assertEquals(expected, filter.filter(topicValue));
    }

}
