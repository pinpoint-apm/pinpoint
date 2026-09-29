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

    private static final String AT = "at ";

    @Override
    public String name() {
        return "java";
    }

    @Override
    public boolean matches(String stackTrace) {
        for (String line : StackTraceLines.trimmed(stackTrace)) {
            if (!line.startsWith(AT)) {
                continue;
            }
            // A JVM frame's parens hold file info, never "file:line:col" (that tail is V8/Node).
            final int parenOpen = line.lastIndexOf('(');
            final int parenClose = line.lastIndexOf(')');
            if (parenOpen < 0 || parenClose <= parenOpen) {
                return false;
            }
            if (V8Location.isLocation(line, parenOpen + 1, parenClose)) {
                return false;
            }
            // a '.' inside the method signature, i.e. after "at " and before the '('
            return line.lastIndexOf('.', parenOpen - 1) > AT.length();
        }
        return false;
    }

    @Override
    public void parse(String stackTrace, StackFrameSink sink) {
        for (String line : StackTraceLines.trimmed(stackTrace)) {
            if (!line.startsWith(AT)) {
                continue;
            }

            final int parenOpen = line.lastIndexOf('(');
            final int parenClose = line.lastIndexOf(')');
            if (parenOpen < 0 || parenClose <= parenOpen) {
                continue;
            }

            // "at <className>.<methodName>(" — both parts must be non-empty
            final int lastDot = line.lastIndexOf('.', parenOpen - 1);
            if (lastDot <= AT.length() || lastDot + 1 >= parenOpen) {
                continue;
            }
            final String className = line.substring(AT.length(), lastDot);
            final String methodName = line.substring(lastDot + 1, parenOpen);

            // "(<fileName>:<lineNumber>)" or "(<fileName>)"
            final int fileStart = parenOpen + 1;
            final int colon = line.lastIndexOf(':', parenClose - 1);
            final String fileName;
            final int lineNumber;
            if (colon >= fileStart) {
                fileName = line.substring(fileStart, colon);
                lineNumber = LineNumbers.parseLineNumber(line, colon + 1, parenClose);
            } else {
                fileName = line.substring(fileStart, parenClose);
                lineNumber = "Native Method".equals(fileName) ? -2 : LineNumbers.UNKNOWN;
            }

            if (!sink.add(new StackFrame(className, fileName, lineNumber, methodName))) {
                return;
            }
        }
    }
}
