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

package com.navercorp.pinpoint.profiler.context;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.bootstrap.context.Trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Taejin Koo
 */
public class ActiveTraceRepository {
    // Object key is ?
    // spanId?, transactionId?, uniqueid;
    private final Map<Object, ActiveTraceInfo> activeTraceInfoMap = new ConcurrentHashMap<Object, ActiveTraceInfo>(16*5, 0.75f, 16*5);

    public ActiveTraceRepository() {
    }

    public void addActiveTrace(Object key, ActiveTraceInfo trace) {
        activeTraceInfoMap.put(key, trace);
    }

    public void removeActiveTrace(Object key) {
        activeTraceInfoMap.remove(key);
    }

    public List<ActiveTraceInfo> collect() {
        final Collection<ActiveTraceInfo> copy = activeTraceInfoMap.values();
        return new ArrayList<ActiveTraceInfo>(copy);
    }

}
