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
 * .NET {@code Exception.ToString()} format:
 * <pre>{@code
 * at Cart.Services.CartService.GetCart(GetCartRequest request) in /app/services/CartService.cs:line 42
 * at lambda_method1(Closure, Object)
 * --- End of stack trace from previous location ---
 * }</pre>
 * Parens hold the parameter list (NOT file info — the JVM parser would misread it as a file
 * name); the optional {@code in file:line N} suffix carries the source location.
 */
public class DotNetStackTraceParser implements StackTraceParser {

    private static final Pattern FRAME = Pattern.compile("^at\\s+(.+?)\\((.*?)\\)(?:\\s+in\\s+(.+):line\\s+(\\d+))?$");
    private static final Pattern IN_LINE_MARKER = Pattern.compile("\\sin\\s.+:line\\s+\\d+");

    private static final String UNKNOWN = "<unknown>";

    @Override
    public String name() {
        return "dotnet";
    }

    @Override
    public boolean matches(String stackTrace) {
        // The " in file:line N" suffix is the unambiguous .NET marker; frames without any source
        // info are left to the language attribute (they are shaped too much like JVM frames).
        return IN_LINE_MARKER.matcher(stackTrace).find();
    }

    @Override
    public void parse(String stackTrace, StackFrameSink sink) {
        for (String line : stackTrace.split("\n")) {
            final String trimmed = line.trim();
            if (!trimmed.startsWith("at ")) {
                continue;
            }
            final Matcher matcher = FRAME.matcher(trimmed);
            if (!matcher.matches()) {
                continue;
            }

            final String signature = matcher.group(1).trim();
            if (!StringUtils.hasLength(signature)) {
                continue;
            }
            final String className;
            final String methodName;
            final int lastDot = signature.lastIndexOf('.');
            if (lastDot > 0 && lastDot < signature.length() - 1) {
                className = signature.substring(0, lastDot);
                methodName = signature.substring(lastDot + 1);
            } else {
                className = UNKNOWN;
                methodName = signature;
            }

            final String fileName = matcher.group(3) != null ? matcher.group(3) : "";
            final int lineNumber = matcher.group(4) != null ? parseInt(matcher.group(4)) : -1;

            if (!sink.add(new StackFrame(className, fileName, lineNumber, methodName))) {
                return;
            }
        }
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
