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

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * V8 (Node.js / Chromium) {@code error.stack} format:
 * <pre>{@code
 * at handler (/app/routes/user.js:10:15)          — named frame
 * at async UserService.load (/app/svc.js:22:9)    — async frame
 * at Layer.handle [as handle_request] (...)       — aliased frame
 * at /app/index.js:3:1                            — anonymous frame
 * at process.processTicksAndRejections (node:internal/process/task_queues:95:5)
 * }</pre>
 * The location tail is {@code file:line:column}; the column is dropped (Pinpoint frames carry a
 * line number only). Parens without a {@code :line:col} tail (e.g. {@code (native)}) keep the
 * content as the file name with line -1.
 */
public class NodeStackTraceParser implements StackTraceParser {

    private static final Pattern LOCATION = Pattern.compile("^(.+):(\\d+):(\\d+)$");
    private static final Pattern PARENS_FRAME = Pattern.compile("^at\\s+(?:async\\s+)?(.+?)\\s+\\((.+)\\)$");
    private static final Pattern BARE_FRAME = Pattern.compile("^at\\s+(?:async\\s+)?([^()].*?):(\\d+):(\\d+)$");

    private static final String ANONYMOUS = "<anonymous>";

    @Override
    public String name() {
        return "node";
    }

    @Override
    public boolean matches(String stackTrace) {
        for (String line : stackTrace.split("\n")) {
            final String trimmed = line.trim();
            if (!trimmed.startsWith("at ")) {
                continue;
            }
            if (BARE_FRAME.matcher(trimmed).matches()) {
                return true;
            }
            final Matcher parens = PARENS_FRAME.matcher(trimmed);
            if (parens.matches() && LOCATION.matcher(parens.group(2)).matches()) {
                return true;
            }
            // e.g. "at Array.map (<anonymous>)" — a built-in frame without a location tail;
            // keep scanning, a later frame decides.
        }
        return false;
    }

    @Override
    public void parse(String stackTrace, StackFrameSink sink) {
        for (String line : stackTrace.split("\n")) {
            final String trimmed = line.trim();
            if (!trimmed.startsWith("at ")) {
                continue;
            }

            final StackFrame frame = parseLine(trimmed);
            if (frame != null && !sink.add(frame)) {
                return;
            }
        }
    }

    private StackFrame parseLine(String trimmed) {
        final Matcher parens = PARENS_FRAME.matcher(trimmed);
        if (parens.matches()) {
            final String function = stripAlias(parens.group(1));
            final String inner = parens.group(2);
            final Matcher location = LOCATION.matcher(inner);
            if (location.matches()) {
                return frame(function, location.group(1), parseInt(location.group(2)));
            }
            // e.g. "(native)", "(node:internal/timers)" — no line info
            return frame(function, inner, -1);
        }

        final Matcher bare = BARE_FRAME.matcher(trimmed);
        if (bare.matches()) {
            return frame(null, bare.group(1), parseInt(bare.group(2)));
        }
        return null;
    }

    /** Drops the "[as alias]" decoration V8 appends to re-bound methods. */
    private static String stripAlias(String function) {
        final int alias = function.indexOf(" [as ");
        return alias > 0 ? function.substring(0, alias) : function;
    }

    private static StackFrame frame(String function, String fileName, int lineNumber) {
        final String className;
        final String methodName;
        if (!StringUtils.hasLength(function)) {
            className = ANONYMOUS;
            methodName = ANONYMOUS;
        } else {
            final int lastDot = function.lastIndexOf('.');
            if (lastDot > 0 && lastDot < function.length() - 1) {
                className = function.substring(0, lastDot);
                methodName = function.substring(lastDot + 1);
            } else {
                className = ANONYMOUS;
                methodName = function;
            }
        }
        return new StackFrame(className, fileName, lineNumber, methodName);
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
