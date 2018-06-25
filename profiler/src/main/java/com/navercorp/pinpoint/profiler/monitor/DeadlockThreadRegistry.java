/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Taejin Koo
 */
public class DeadlockThreadRegistry implements DeadlockThreadLocator {

    private static final Object DUMMY_VALUE = new Object();

    private final ConcurrentMap<Long, Object> deadlockedThreadIdMap = new ConcurrentHashMap<Long, Object>();

    boolean addDeadlockedThread(long threadId) {
        Object oldValue = deadlockedThreadIdMap.putIfAbsent(threadId, DUMMY_VALUE);
        return oldValue == null;
    }

    @Override
    public Set<Long> getDeadlockedThreadIdSet() {
        final ConcurrentMap<Long, Object> deadlockedThreadIdMap = this.deadlockedThreadIdMap;
        if (deadlockedThreadIdMap.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<Long>(deadlockedThreadIdMap.keySet());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeadlockThreadRegistry{");
        sb.append("deadlockedThreadIdSet=").append(deadlockedThreadIdMap.keySet());
        sb.append('}');
        return sb.toString();
    }

}
