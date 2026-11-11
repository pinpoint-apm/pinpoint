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

package com.navercorp.pinpoint.channel.redis.stream;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisStreamConfigTest {

    @Test
    void listenerContainerIsStartedAndStoppedByTheContext() {
        final RedisStreamConfig config = new RedisStreamConfig();
        final RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                config.streamMessageListenerContainer(connectionFactory, new SimpleAsyncTaskExecutor());
        // not started by the bean method
        assertThat(container.isRunning()).isFalse();
        assertThat(container.isAutoStartup()).isTrue();

        final GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("streamMessageListenerContainer", StreamMessageListenerContainer.class, () -> container);
        try {
            context.refresh();
            assertThat(container.isRunning()).isTrue();
        } finally {
            context.close();
        }
        assertThat(container.isRunning()).isFalse();
    }
}
