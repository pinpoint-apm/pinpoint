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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.util.Utf8;
import io.micrometer.core.instrument.Counter;

/**
 * Bounds for the free-form exception fields a third-party exporter supplies (type, message, route
 * template, flattened stack trace). Shared by the span-event path ({@link OtlpExceptionMapper}) and
 * the log-record path so both signals store the same shape and neither lets an oversized or hostile
 * value through to the parsers or the store. Every method takes the counter to increment when it
 * actually changed the value, so each caller reports under its own metric namespace.
 */
public final class ExceptionFieldLimits {

    private ExceptionFieldLimits() {
    }

    /**
     * Truncates {@code value} to at most {@code maxBytes} UTF-8 bytes (never splitting a multi-byte
     * character) and increments {@code counter} when truncation actually occurred. Returns the
     * original reference when already within the limit.
     *
     * <p>If truncation would yield an empty string for a non-empty input (i.e. {@code maxBytes} is
     * smaller than the first code point's UTF-8 length), the original is kept instead: an empty
     * value would violate the non-empty className/methodName contract of the stack frame BO.
     */
    public static String cap(String value, int maxBytes, Counter counter) {
        final String truncated = Utf8.truncate(value, maxBytes);
        if (truncated == null) {
            return value; // already within the limit
        }
        if (truncated.isEmpty() && !value.isEmpty()) {
            return value; // would drop the whole value; keep the original rather than emit ""
        }
        counter.increment();
        return truncated;
    }

    /**
     * Normalizes a route template before it is stored as {@code uriTemplate}: a template carries no
     * query string or fragment, so anything from the first {@code ?} or {@code #} on is dropped
     * (a buggy SDK that sends the raw URL would otherwise persist tokens), control characters are
     * removed, and the result is capped at {@code maxBytes}. Never returns null; an empty result
     * means "no route" (the store's convention). {@code counter} is incremented once when anything
     * was cut.
     */
    public static String sanitizeUriTemplate(String value, int maxBytes, Counter counter) {
        if (value == null) {
            return "";
        }
        boolean changed = false;
        String result = value;
        final int cut = firstIndexOf(result, '?', '#');
        if (cut >= 0) {
            result = result.substring(0, cut);
            changed = true;
        }
        if (hasControlChar(result)) {
            result = stripControlChars(result);
            changed = true;
        }
        final String truncated = Utf8.truncate(result, maxBytes);
        if (truncated != null) {
            result = truncated;
            changed = true;
        }
        if (changed) {
            counter.increment();
        }
        return result;
    }

    /**
     * Makes a flattened stack trace safe to hand to the regex-based language parsers, whose worst
     * case is quadratic in the length of a single line: the whole text is cut at {@code maxChars}
     * (at the last line break before it, so no half frame is parsed) and every line longer than
     * {@code lineMaxChars} is cut to that length. Both bounds are {@code <= 0} for unlimited. A
     * single O(n) pass; {@code bytesCounter} / {@code lineCounter} are incremented once per call when
     * the respective bound applied.
     */
    public static String boundStackTrace(String stackTrace, int maxChars, int lineMaxChars,
                                         Counter bytesCounter, Counter lineCounter) {
        if (stackTrace == null || stackTrace.isEmpty()) {
            return stackTrace;
        }
        String text = stackTrace;
        if (maxChars > 0 && text.length() > maxChars) {
            final int lastBreak = text.lastIndexOf('\n', maxChars);
            text = text.substring(0, lastBreak > 0 ? lastBreak : maxChars);
            bytesCounter.increment();
        }
        if (lineMaxChars <= 0 || !hasLongLine(text, lineMaxChars)) {
            return text;
        }
        final StringBuilder sb = new StringBuilder(Math.min(text.length(), lineMaxChars * 64));
        int start = 0;
        while (start <= text.length()) {
            int end = text.indexOf('\n', start);
            if (end < 0) {
                end = text.length();
            }
            final int lineEnd = Math.min(end, start + lineMaxChars);
            sb.append(text, start, lineEnd);
            if (end == text.length()) {
                break;
            }
            sb.append('\n');
            start = end + 1;
        }
        lineCounter.increment();
        return sb.toString();
    }

    private static boolean hasLongLine(String text, int lineMaxChars) {
        int start = 0;
        while (true) {
            int end = text.indexOf('\n', start);
            if (end < 0) {
                end = text.length();
            }
            if (end - start > lineMaxChars) {
                return true;
            }
            if (end == text.length()) {
                return false;
            }
            start = end + 1;
        }
    }

    private static int firstIndexOf(String s, char a, char b) {
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == a || c == b) {
                return i;
            }
        }
        return -1;
    }

    private static boolean hasControlChar(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (isControl(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static String stripControlChars(String s) {
        final StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (!isControl(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isControl(char c) {
        return c < 0x20 || c == 0x7f;
    }
}
