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
 * Last-resort parser for unrecognized formats (Ruby, PHP, Rust, custom SDKs, ...): each non-blank
 * line becomes one frame with the raw line as the class name. This keeps two things working that
 * an empty frame list would break: the Error Analysis detail view still shows the original text,
 * and the stack-trace grouping hash stays distinctive instead of collapsing every unparsed
 * exception into one shared "empty stack" group.
 *
 * <p>Hex addresses ({@code 0x...}) are scrubbed before hashing-relevant storage — they vary per
 * process and would otherwise split one logical error into many groups.
 */
public class RawStackTraceParser implements StackTraceParser {

    private static final Pattern HEX_ADDRESS = Pattern.compile("0x[0-9a-fA-F]+");

    private static final String UNKNOWN_METHOD = "?";

    @Override
    public String name() {
        return "raw-fallback";
    }

    @Override
    public boolean matches(String stackTrace) {
        return true;
    }

    @Override
    public void parse(String stackTrace, StackFrameSink sink) {
        for (String line : stackTrace.split("\n")) {
            final String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            final String scrubbed = HEX_ADDRESS.matcher(trimmed).replaceAll("0x?");
            if (!sink.add(new StackFrame(scrubbed, "", -1, UNKNOWN_METHOD))) {
                return;
            }
        }
    }
}
