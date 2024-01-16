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
package com.navercorp.pinpoint.log.web.dao;

import com.navercorp.pinpoint.channel.service.client.FluxChannelServiceClient;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.LogPile;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Flux;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class LiveTailDaoImpl implements LiveTailDao {

    private final RedisTemplate<String, String> redis;
    private final FluxChannelServiceClient<FileKey, LogPile> client;

    public LiveTailDaoImpl(
            RedisTemplate<String, String> redis,
            FluxChannelServiceClient<FileKey, LogPile> client
    ) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public Flux<LogPile> tail(FileKey fileKey) {
        return this.client.request(fileKey);
    }

    @Override
    public List<FileKey> getFileKeys() {
        Set<String> keys = this.redis.keys("log:files:*");
        if (keys == null) {
            return List.of();
        }
        List<FileKey> fileKeys = new ArrayList<>(keys.size() * 8);
        for (String key: keys) {
            fileKeys.addAll(getFileKeysFromRedis(key));
        }
        return fileKeys;
    }

    private List<FileKey> getFileKeysFromRedis(String redisKey) {
        String value = this.redis.opsForValue().get(redisKey);
        if (value == null) {
            return List.of();
        }
        return parseFileKeyList(value);
    }

    private List<FileKey> parseFileKeyList(String raw) {
        List<FileKey> keys =  new ArrayList<>(8);
        for (String fileKey: raw.split("\r\n")) {
            try {
                keys.add(FileKey.parse(fileKey));
            } catch (ParseException ignored) {
            }
        }
        return keys;
    }

}
