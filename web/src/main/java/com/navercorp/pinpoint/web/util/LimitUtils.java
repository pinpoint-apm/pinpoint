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

package com.navercorp.pinpoint.web.util;

/**
 * @author emeroad
 */
public final class LimitUtils {
    public static final int MAX = 10000;

    private LimitUtils() {
    }

    public static int checkRange(final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("negative limit:" + limit);
        }
        if (limit > MAX) {
            return MAX;
        }
        return limit;
    }
}
