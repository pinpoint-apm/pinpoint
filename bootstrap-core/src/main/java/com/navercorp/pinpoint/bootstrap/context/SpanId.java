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

package com.navercorp.pinpoint.bootstrap.context;

import java.util.Random;

import com.navercorp.pinpoint.bootstrap.util.jdk.ThreadLocalRandomUtils;

/**
 * @author emeroad
 */
public class SpanId {

    public static final long NULL = -1;

//    private static final Random seed = new Random();

    public static long newSpanId() {
        final Random random = getRandom();

        return createSpanId(random);
    }

    // Changed to ThreadLocalRandom because unique value per thread will be enough.
    // If you need to change Random implementation, modify this method.
    private static Random getRandom() {
        return ThreadLocalRandomUtils.current();
    }

    private static long createSpanId(Random seed) {
        long id = seed.nextLong();
        while (id == NULL) {
            id = seed.nextLong();
        }
        return id;
    }

    public static long nextSpanID(long spanId, long parentSpanId) {
        final Random seed = getRandom();

        long newId = createSpanId(seed);
        while (newId == spanId || newId == parentSpanId) {
            newId = createSpanId(seed);
        }
        return newId;
    }
}
