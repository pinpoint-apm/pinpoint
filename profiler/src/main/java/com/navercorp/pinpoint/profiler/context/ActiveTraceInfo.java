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

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author Taejin Koo
 */
public class ActiveTraceInfo {

    private final long startTime;
    private final CopyOnWriteArrayList<String> relatedThreadNameList;
    private final long spanId;

    public ActiveTraceInfo(long spanId) {
        this.startTime = System.currentTimeMillis();
        this.relatedThreadNameList = new CopyOnWriteArrayList<String>();

        this.spanId = spanId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void addThreadName(String activeThreadName) {
        this.relatedThreadNameList.add(activeThreadName);
    }

    public Iterator<String> getRelatedThreadNameList() {
        return relatedThreadNameList.iterator();
    }

    public long getSpanId() {
        return spanId;
    }

}
