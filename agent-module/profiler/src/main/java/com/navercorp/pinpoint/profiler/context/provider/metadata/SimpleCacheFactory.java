/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.metadata;

import com.navercorp.pinpoint.profiler.cache.IdAllocator;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleCacheFactory {

    private final IdAllocator.ID_TYPE type;

    public SimpleCacheFactory(IdAllocator.ID_TYPE type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    public <T> SimpleCache<T> newSimpleCache() {
        IdAllocator idAllocator = newIdAllocator(type, 1);
        return new SimpleCache<>(idAllocator);
    }

    public <T> SimpleCache<T> newSimpleCache(int size) {
        IdAllocator idAllocator = newIdAllocator(type, size);
        return new SimpleCache<>(idAllocator, size);
    }

    private IdAllocator newIdAllocator(IdAllocator.ID_TYPE type, int size) {
        switch (type) {
            case BYPASS:
                return new IdAllocator.BypassAllocator(size);
            case ZIGZAG:
                return new IdAllocator.ZigZagAllocator(size);
        }
        throw new RuntimeException("Unknown SimpleCache.ID_TYPE:" + type);
    }
}

