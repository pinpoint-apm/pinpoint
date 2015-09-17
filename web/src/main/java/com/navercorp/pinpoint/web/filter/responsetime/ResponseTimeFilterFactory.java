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

package com.navercorp.pinpoint.web.filter.responsetime;

/**
 * @author emeroad
 */
public class ResponseTimeFilterFactory {
    private final Long from;
    private final Long to;

    public ResponseTimeFilterFactory(Long from, Long to) {
        this.from = from;
        this.to = to;
    }

    public ResponseTimeFilter createFilter() {
        if (from == null && to == null) {
            return new AcceptResponseTimeFilter();
        }
        // TODO default value is 0 or Long.MIN_VALUE ??
        final long fromLong = defaultLong(from, Long.MIN_VALUE);
        final long toLong = defaultLong(to, Long.MAX_VALUE);
        return new DefaultResponseTimeFilter(fromLong, toLong);
    }

    private Long defaultLong(Long value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
