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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPython {@code traceback.format_exception} format:
 * <pre>{@code
 * Traceback (most recent call last):
 *   File "/app/main.py", line 10, in handler
 *     do_work()
 *   File "/app/svc.py", line 3, in do_work
 * ValueError: boom
 * }</pre>
 * Frame mapping: methodName = the {@code in} function ({@code <module>} when absent), fileName =
 * the quoted path, className = the file's module name (basename without {@code .py}).
 *
 * <p>Python prints frames outermost-first ("most recent call last") — the reverse of the JVM
 * convention every other consumer of these frames assumes. Frames are reversed to
 * innermost-first so the detail view reads top-down to the throw site and the stack-trace hash
 * groups consistently across languages. (When the frame cap truncates, the innermost frames are
 * the ones kept.) Chained-exception separators ("The above exception was the direct cause ...")
 * are not split yet — chain decomposition is the planned follow-up.
 */
public class PythonStackTraceParser implements StackTraceParser {

    private static final Pattern FRAME = Pattern.compile("^File\\s+\"(.+)\",\\s+line\\s+(\\d+)(?:,\\s+in\\s+(.+))?$");
    private static final String TRACEBACK_HEADER = "Traceback (most recent call last)";
    private static final String MODULE = "<module>";

    @Override
    public String name() {
        return "python";
    }

    @Override
    public boolean matches(String stackTrace) {
        if (stackTrace.contains(TRACEBACK_HEADER)) {
            return true;
        }
        for (String line : stackTrace.split("\n")) {
            if (FRAME.matcher(line.trim()).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void parse(String stackTrace, StackFrameSink sink) {
        final List<StackFrame> outermostFirst = new ArrayList<>();
        for (String line : stackTrace.split("\n")) {
            final Matcher matcher = FRAME.matcher(line.trim());
            if (!matcher.matches()) {
                continue;
            }

            final String fileName = matcher.group(1);
            final int lineNumber = parseInt(matcher.group(2));
            final String function = matcher.group(3);
            final String methodName = StringUtils.hasLength(function) ? function : MODULE;
            outermostFirst.add(new StackFrame(moduleName(fileName), fileName, lineNumber, methodName));
        }

        for (int i = outermostFirst.size() - 1; i >= 0; i--) {
            if (!sink.add(outermostFirst.get(i))) {
                return;
            }
        }
    }

    /** {@code /app/pkg/main.py} → {@code main}; falls back to {@code <module>} when empty. */
    private static String moduleName(String path) {
        int cut = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        String base = cut >= 0 ? path.substring(cut + 1) : path;
        if (base.endsWith(".py")) {
            base = base.substring(0, base.length() - 3);
        }
        return StringUtils.hasLength(base) ? base : MODULE;
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
