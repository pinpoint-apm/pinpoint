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
package com.navercorp.pinpoint.channel.redis.stream;

import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveStreamOperations;

import java.util.Map;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisStreamPubChannel implements PubChannel {

    private static final int STREAM_MAX_LEN = 32;
    private static final String STREAM_RECORD_KEY = "content";

    private final ReactiveStreamOperations<String, String, String> streamOps;
    private final String key;

    RedisStreamPubChannel(ReactiveStreamOperations<String, String, String> streamOps, String key) {
        this.streamOps = Objects.requireNonNull(streamOps, "streamOps");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public void publish(byte[] bytes) {
        final String content = BytesUtils.toString(bytes);
        final MapRecord<String, String, String> record =
                MapRecord.create(this.key, Map.of(STREAM_RECORD_KEY, content));
        this.streamOps.add(record)
                .flatMap(el -> this.streamOps.trim(this.key, STREAM_MAX_LEN, false))
                .subscribe();
    }

}
