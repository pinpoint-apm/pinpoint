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

package com.navercorp.pinpoint.otlp.trace.collector.mapper.stacktrace;

import java.util.regex.Matcher;

/**
 * Parses frame line numbers in place, without cutting a substring for the digits.
 */
final class LineNumbers {

    /** The {@link StackFrame} line number for a missing or malformed token (e.g. "Foo.java:??"). */
    static final int UNKNOWN = -1;

    private LineNumbers() {
    }

    /**
     * @return the number in {@code value[from..to)}, or {@link #UNKNOWN} when it is not a decimal integer
     */
    static int parseLineNumber(String value, int from, int to) {
        try {
            return Integer.parseInt(value, from, to, 10);
        } catch (NumberFormatException e) {
            return UNKNOWN;
        }
    }

    /**
     * @param matcher a matcher over {@code value} after a successful match
     * @return the number captured by {@code group}, or {@link #UNKNOWN} when the group did not participate
     * or is not a decimal integer
     */
    static int parseLineNumber(String value, Matcher matcher, int group) {
        final int from = matcher.start(group);
        if (from < 0) {
            return UNKNOWN;
        }
        return parseLineNumber(value, from, matcher.end(group));
    }
}
