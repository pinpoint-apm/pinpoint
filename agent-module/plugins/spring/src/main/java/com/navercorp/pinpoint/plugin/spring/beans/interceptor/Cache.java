/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class Cache {
    private static final int SHARD_SIZE_LIMIT = 64;
    private static final int SHARD_NUM = 13;

    private final Shard[] shards;

    public Cache() {
        shards = new Shard[SHARD_NUM];

        for (int i = 0; i < SHARD_NUM; i++) {
            shards[i] = new Shard();
        }
    }

    public void clear() {
        for (int i = 0; i < SHARD_NUM; i++) {
            shards[i].clear();
        }
    }

    public boolean contains(final String className) {
        final Shard shard = getShard(className);

        synchronized (shard) {
            return shard.containsKey(className);
        }
    }

    public void put(final String className) {

        final Shard shard = getShard(className);
        synchronized (shard) {
            shard.put(className, Boolean.TRUE);
        }
    }

    private Shard getShard(final String className) {
        int idx = className.hashCode() % SHARD_NUM;

        if (idx < 0) {
            idx += SHARD_NUM;
        }

        return shards[idx];
    }

    @SuppressWarnings("serial")
    private static final class Shard extends LinkedHashMap<String, Boolean> {

        @Override
        protected boolean removeEldestEntry(Entry<String, Boolean> eldest) {
            return size() > SHARD_SIZE_LIMIT;
        }

    }
}
