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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DeadlockThreadRegistry implements DeadlockThreadLocator {

    private final Set<Long> deadlockedThreadIdSet = new HashSet<Long>();

    boolean addDeadlockedThread(long threadId) {
        return deadlockedThreadIdSet.add(threadId);
    }

    @Override
    public Set<Long> getDeadlockedThreadIdSet() {
        Set<Long> copied = new HashSet<Long>(deadlockedThreadIdSet.size());
        copied.addAll(deadlockedThreadIdSet);
        return copied;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeadlockThreadRegistry{");
        sb.append("deadlockedThreadIdSet=").append(deadlockedThreadIdSet);
        sb.append('}');
        return sb.toString();
    }

}
