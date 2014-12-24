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
    // FIXME 매핑정보 매번 저장하지 말고 30~50초 주기로 한 개만 저장되도록 변경.
    // OOM 위험성이 있으니 LRU로 변경할지 검토할것?
    private final ConcurrentMap<T, AtomicLong> cache = new ConcurrentHashMap<T, AtomicLong>(1024, 0.75f, 32);


    public boolean update(final T cacheKey, final long time) {
        if (cacheKey == null) {
            throw new NullPointerException("cacheKey must not be null");
        }
        final AtomicLong hitSlot = cache.get(cacheKey);
        if (hitSlot == null ) {
            final AtomicLong newTime = new AtomicLong(time);
            final AtomicLong oldTime = cache.putIfAbsent(cacheKey, newTime);
            if (oldTime == null) {
                // 자신이 새롭게 넣는 주체이다.
                return true;
            } else {
                // 이미 키가 존재한다.
                return updateTime(time, oldTime);
            }
        } else {
            // 이미 키가 존재할 경우 update한다.
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
