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

import com.google.common.base.Splitter;

/**
 * Line iteration shared by the parsers. The {@link Splitter} is lazy, so a {@code matches()} that
 * returns on the first recognized line and a {@code parse()} that stops at the sink's frame cap
 * never materialize the remaining lines. Trimming before each line is cut avoids the second copy
 * that {@code split("\\n")} followed by {@code trim()} makes for every indented line.
 */
final class StackTraceLines {

    private static final Splitter LINES = Splitter.on('\n').trimResults().omitEmptyStrings();

    private StackTraceLines() {
    }

    /**
     * @return the non-empty, trimmed lines of {@code stackTrace}, produced on demand
     */
    static Iterable<String> trimmed(String stackTrace) {
        return LINES.split(stackTrace);
    }
}
