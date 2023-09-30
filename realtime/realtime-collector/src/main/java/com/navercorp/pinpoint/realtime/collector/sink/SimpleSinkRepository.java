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
package com.navercorp.pinpoint.realtime.collector.sink;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author youngjin.kim2
 */
public class SimpleSinkRepository<T> implements SinkRepository<T> {

    private final Map<Long, T> map = new ConcurrentHashMap<>();
    private final AtomicLong idCounter;

    SimpleSinkRepository(AtomicLong idCounter) {
        this.idCounter = Objects.requireNonNull(idCounter, "idCounter");
    }

    @Override
    public long put(T sink) {
        long id = idCounter.incrementAndGet();
        this.map.put(id, sink);
        return id;
    }

    @Override
    public T get(long id) {
        return this.map.get(id);
    }

    @Override
    public void invalidate(long id) {
        this.map.remove(id);
    }

}
