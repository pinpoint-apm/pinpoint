/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.collector.service.state;

import com.navercorp.pinpoint.channel.serde.Serde;
import com.navercorp.pinpoint.realtime.vo.CollectorState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class RedisCollectorStatePublisherServiceTest {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_RESULT = "testResult";

    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> redisValueOperations;
    @Mock Serde<CollectorState> serde;
    @Test
    public void test() throws IOException {
        CollectorState state = new CollectorState(List.of());

        doReturn(TEST_RESULT.getBytes()).when(serde).serializeToByteArray(eq(state));
        doReturn(redisValueOperations).when(redisTemplate).opsForValue();
        doReturn(true).when(redisTemplate).expire(eq(TEST_KEY), any());
        doNothing().when(redisValueOperations).set(eq(TEST_KEY), eq(TEST_RESULT));

        RedisCollectorStatePublisherService service = new RedisCollectorStatePublisherService(
                redisTemplate,
                serde,
                TEST_KEY,
                Duration.ofSeconds(5)
        );
        service.publish(state);

        verify(redisTemplate).expire(eq(TEST_KEY), any());
        verify(redisValueOperations).set(eq(TEST_KEY), eq(TEST_RESULT));
    }

}
