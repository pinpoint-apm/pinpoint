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
 * Go {@code runtime/debug.Stack()} format — two-line pairs:
 * <pre>{@code
 * goroutine 1 [running]:
 * github.com/acme/pkg.(*Service).Handle(0xc000010000)
 *         /app/service.go:42 +0x5e
 * main.main()
 *         /app/main.go:10 +0x20
 * }</pre>
 * The trailing {@code +0x...} program-counter offset is dropped: it varies per build, and keeping
 * it would destabilize the stack-trace grouping hash across deployments.
 */
public class GoStackTraceParser implements StackTraceParser {

    private static final Pattern GOROUTINE_HEADER = Pattern.compile("^goroutine\\s+\\d+\\s+\\[.*\\]:$");
    private static final Pattern FILE_LINE = Pattern.compile("^(.+\\.go):(\\d+)(?:\\s+\\+0x[0-9a-fA-F]+)?$");
    private static final String CREATED_BY = "created by ";

    @Override
    public String name() {
        return "go";
    }

    @Override
    public boolean matches(String stackTrace) {
        for (String line : stackTrace.split("\n")) {
            final String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (GOROUTINE_HEADER.matcher(trimmed).matches() || FILE_LINE.matcher(trimmed).matches()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void parse(String stackTrace, StackFrameSink sink) {
        final String[] lines = stackTrace.split("\n");
        for (int i = 0; i < lines.length - 1; i++) {
            final String funcLine = lines[i].trim();
            if (funcLine.isEmpty() || GOROUTINE_HEADER.matcher(funcLine).matches()) {
                continue;
            }
            final Matcher fileLine = FILE_LINE.matcher(lines[i + 1].trim());
            if (!fileLine.matches()) {
                continue;
            }

            final StackFrame frame = frame(funcLine, fileLine.group(1), parseInt(fileLine.group(2)));
            if (frame != null && !sink.add(frame)) {
                return;
            }
            i++; // consume the file line of this pair
        }
    }

    private static StackFrame frame(String funcLine, String fileName, int lineNumber) {
        String signature = funcLine;
        if (signature.startsWith(CREATED_BY)) {
            signature = signature.substring(CREATED_BY.length());
        }
        // Drop the argument list: "pkg.(*T).Method(0xc000010000)" → "pkg.(*T).Method".
        final int argsParen = signature.lastIndexOf('(');
        if (argsParen > 0 && signature.endsWith(")")) {
            // Receiver parens like "(*T)" sit before the last dot; the args paren is after it.
            final int lastDot = signature.lastIndexOf('.');
            if (argsParen > lastDot) {
                signature = signature.substring(0, argsParen);
            }
        }

        final int lastDot = signature.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= signature.length() - 1) {
            return null;
        }
        final String className = signature.substring(0, lastDot);
        final String methodName = signature.substring(lastDot + 1);
        if (!StringUtils.hasLength(className) || !StringUtils.hasLength(methodName)) {
            return null;
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
