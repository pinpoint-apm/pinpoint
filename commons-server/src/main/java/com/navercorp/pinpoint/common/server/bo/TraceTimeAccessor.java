/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.io.SpanVersion;

import java.util.concurrent.TimeUnit;

/**
 * Accessor for the raw span timestamp fields.
 * Pre-V3 spans persist epoch millis, TRACE_V3 spans persist epoch nanos;
 * the raw {@code long} stays on the owning Bo and this enum converts it on access.
 *
 * @author Woonduk Kang(emeroad)
 */
public enum TraceTimeAccessor {

    MILLIS {
        @Override
        public long toMillis(long timestamp) {
            return timestamp;
        }

        @Override
        public long toNanos(long timestamp) {
            return TimeUnit.MILLISECONDS.toNanos(timestamp);
        }
    },
    NANOS {
        @Override
        public long toMillis(long timestamp) {
            return TimeUnit.NANOSECONDS.toMillis(timestamp);
        }

        @Override
        public long toNanos(long timestamp) {
            return timestamp;
        }
    };

    public static TraceTimeAccessor ofVersion(int version) {
        if (version == SpanVersion.TRACE_V3) {
            return NANOS;
        }
        return MILLIS;
    }

    public abstract long toMillis(long timestamp);

    public abstract long toNanos(long timestamp);
}
