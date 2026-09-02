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

/**
 * Java/JVM {@code Throwable.printStackTrace} format:
 * <pre>{@code at com.example.Service.handle(Service.java:42)}</pre>
 * with {@code (Native Method)} → line -2 and {@code (Unknown Source)} → line -1.
 *
 * <p>"Caused by:" sections are NOT split into separate exceptions yet — their frames merge into
 * one flat list (the pre-existing semantics). Chain decomposition is a planned follow-up; this
 * parser is where the "Caused by:" boundary would be detected.
 */
public class JavaStackTraceParser implements StackTraceParser {

    @Override
    public String name() {
        return "java";
    }

    @Override
    public boolean matches(String stackTrace) {
        for (String line : stackTrace.split("\n")) {
            final String trimmed = line.trim();
            if (!trimmed.startsWith("at ")) {
                continue;
            }
            // A JVM frame's parens hold file info, never "file:line:col" (that tail is V8/Node).
            final String element = trimmed.substring(3);
            final int parenOpen = element.lastIndexOf('(');
            final int parenClose = element.lastIndexOf(')');
            if (parenOpen < 0 || parenClose <= parenOpen) {
                return false;
            }
            final String fileInfo = element.substring(parenOpen + 1, parenClose);
            if (fileInfo.matches(".*:\\d+:\\d+$")) {
                return false;
            }
            return element.substring(0, parenOpen).lastIndexOf('.') > 0;
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

            final String element = trimmed.substring(3);
            final int parenOpen = element.lastIndexOf('(');
            final int parenClose = element.lastIndexOf(')');
            if (parenOpen < 0 || parenClose <= parenOpen) {
                continue;
            }

            final String methodSignature = element.substring(0, parenOpen);
            final String fileInfo = element.substring(parenOpen + 1, parenClose);

            final int lastDot = methodSignature.lastIndexOf('.');
            if (lastDot < 0) {
                continue;
            }

            final String className = methodSignature.substring(0, lastDot);
            final String methodName = methodSignature.substring(lastDot + 1);
            if (!StringUtils.hasLength(className) || !StringUtils.hasLength(methodName)) {
                continue;
            }

            final String fileName;
            final int lineNumber;
            final int colonIdx = fileInfo.lastIndexOf(':');
            if (colonIdx >= 0) {
                fileName = fileInfo.substring(0, colonIdx);
                int parsed;
                try {
                    parsed = Integer.parseInt(fileInfo.substring(colonIdx + 1));
                } catch (NumberFormatException e) {
                    // malformed line token (e.g. "Foo.java:??") -> unknown, per the StackFrame contract
                    parsed = -1;
                }
                lineNumber = parsed;
            } else {
                fileName = fileInfo;
                lineNumber = "Native Method".equals(fileInfo) ? -2 : -1;
            }

            if (!sink.add(new StackFrame(className, fileName, lineNumber, methodName))) {
                return;
            }
        }
    }
}
