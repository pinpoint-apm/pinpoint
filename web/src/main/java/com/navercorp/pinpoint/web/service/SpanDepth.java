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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.calltree.span.SpanAlign;

/**
 * @author emeroad
 */
public class SpanDepth {
    private final SpanAlign spanAlign;
    private final int id;
    // needed for finding gap
    private final long lastExecuteTime;

    public SpanDepth(SpanAlign spanAlign, int id, long lastExecuteTime) {
        if (spanAlign == null) {
            throw new NullPointerException("spanAlign must not be null");
        }
        this.spanAlign = spanAlign;
        this.id = id;
        this.lastExecuteTime = lastExecuteTime;
    }

    public SpanAlign getSpanAlign() {
        return spanAlign;
    }

    public int getId() {
        return id;
    }

    public long getLastExecuteTime() {
        return lastExecuteTime;
    }
}
