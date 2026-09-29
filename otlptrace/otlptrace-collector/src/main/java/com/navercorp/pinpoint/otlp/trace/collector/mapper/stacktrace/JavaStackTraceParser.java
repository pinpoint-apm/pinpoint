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

import java.util.regex.Pattern;

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
    // "file:line:col" is the V8/Node location tail; a JVM frame never carries a column
    private static final Pattern V8_LOCATION_TAIL = Pattern.compile(".*:\\d+:\\d+$");

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
            if (isV8LocationTail(line, parenOpen + 1, parenClose)) {
                return false;
            }
            // a '.' inside the method signature, i.e. after "at " and before the '('
            return line.lastIndexOf('.', parenOpen - 1) > AT.length();
        }
        return false;
    }

    /**
     * @return whether {@code line[from..to)} is a {@code file:line:col} location, matched in place
     */
    static boolean isV8LocationTail(String line, int from, int to) {
        return V8_LOCATION_TAIL.matcher(line).region(from, to).matches();
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
                lineNumber = parseInt(line, colon + 1, parenClose);
            } else {
                fileName = line.substring(fileStart, parenClose);
                lineNumber = "Native Method".equals(fileName) ? -2 : -1;
            }

            if (!sink.add(new StackFrame(className, fileName, lineNumber, methodName))) {
                return;
            }
        }
    }

    /**
     * Parses {@code value[from..to)} in place, without a substring.
     *
     * @return the number, or -1 for a malformed line token (e.g. "Foo.java:??"), per the StackFrame contract
     */
    private static int parseInt(String value, int from, int to) {
        try {
            return Integer.parseInt(value, from, to, 10);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
