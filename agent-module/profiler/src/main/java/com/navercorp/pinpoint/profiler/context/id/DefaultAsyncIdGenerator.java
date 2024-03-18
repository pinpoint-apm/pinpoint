/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.id;

import com.google.inject.Inject;
import com.navercorp.pinpoint.profiler.context.AsyncId;
import com.navercorp.pinpoint.profiler.context.DefaultAsyncId;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultAsyncIdGenerator implements AsyncIdGenerator {

    private final AtomicInteger asyncId = new AtomicInteger();

    @Inject
    public DefaultAsyncIdGenerator() {
    }

    @Override
    public int nextAsyncId() {
        final int id = asyncId.incrementAndGet();
        if (id == -1) {
            return asyncId.incrementAndGet();
        }
        else {
            return id;
        }
    }

    @Override
    public AsyncId newAsyncId() {
        final int asyncId = nextAsyncId();
        return new DefaultAsyncId(asyncId);
    }
}
