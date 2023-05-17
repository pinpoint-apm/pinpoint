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
package com.navercorp.pinpoint.web.realtime.activethread.count.dao;

import com.google.common.cache.Cache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * @author youngjin.kim2
 */
public class CachedFetcherFactory<K, V> implements FetcherFactory<K, V> {

    private static final Logger logger = LogManager.getLogger(CachedFetcherFactory.class);
    private final FetcherFactory<K, V> child;

    private final Cache<K, Fetcher<V>> cache;

    public CachedFetcherFactory(FetcherFactory<K, V> child, Cache<K, Fetcher<V>> cache) {
        this.child = Objects.requireNonNull(child, "child");
        this.cache = Objects.requireNonNull(cache, "cache");
    }

    @Override
    public Fetcher<V> getFetcher(K key) {
        try {
            return cache.get(key, () -> this.child.getFetcher(key));
        } catch (ExecutionException e) {
            logger.error("Failed to getFetcher: {}", key, e);
            throw new RuntimeException("Failed to getFetcher: " + key, e);
        }
    }

}
