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

package com.navercorp.pinpoint.otlp.trace.collector.util;

/**
 * Renders a client-supplied value so it can be echoed into a log line or an error message: control
 * characters are replaced by their {@code \\uXXXX} escape (so a CR/LF cannot forge a new log line,
 * and the attempt stays visible) and the result is abbreviated to {@code maxLength} characters (so a
 * multi-megabyte value cannot bloat the log or the response). Only for values that failed
 * validation — anything that passed the id validators is already restricted to a safe alphabet.
 */
public final class LogSafe {

    public static final int DEFAULT_MAX_LENGTH = 256;
    private static final String ELLIPSIS = "...";

    private LogSafe() {
    }

    public static String value(String value) {
        return value(value, DEFAULT_MAX_LENGTH);
    }

    public static String value(String value, int maxLength) {
        if (value == null) {
            return "null";
        }
        final StringBuilder sb = new StringBuilder(Math.min(value.length(), maxLength) + 8);
        int kept = 0;
        for (int i = 0; i < value.length() && kept < maxLength; i++) {
            final char c = value.charAt(i);
            if (c < 0x20 || c == 0x7f) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
            kept++;
        }
        if (kept < value.length()) {
            sb.append(ELLIPSIS).append("(len=").append(value.length()).append(')');
        }
        return sb.toString();
    }
}
