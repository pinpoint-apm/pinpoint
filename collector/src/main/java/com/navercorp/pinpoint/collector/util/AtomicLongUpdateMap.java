/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author emeroad
 */
public class AtomicLongUpdateMap<T> {
    // FIXME consider to save a mapping information at each 30 ~ 50 seconds not to do at each time.
    // consider to change to LRU due to OOM risk

    private final ConcurrentMap<T, AtomicLong> cache = new ConcurrentHashMap<>(1024, 0.75f, 32);


    public boolean update(final T cacheKey, final long time) {
        if (cacheKey == null) {
            throw new NullPointerException("cacheKey");
        }
        final AtomicLong hitSlot = cache.get(cacheKey);
        if (hitSlot == null ) {
            final AtomicLong newTime = new AtomicLong(time);
            final AtomicLong oldTime = cache.putIfAbsent(cacheKey, newTime);
            if (oldTime == null) {

                return true;
            } else {
                // the cachekey already exists
                return updateTime(time, oldTime);
            }
        } else {
            // update if the cachekey already exists
            return updateTime(time, hitSlot);
        }
    }

    private boolean updateTime(final long newTime, final AtomicLong oldTime) {
        final long oldLong = oldTime.get();
        if (newTime > oldLong) {
            return oldTime.compareAndSet(oldLong, newTime);
        }
        return false;
    }
}
