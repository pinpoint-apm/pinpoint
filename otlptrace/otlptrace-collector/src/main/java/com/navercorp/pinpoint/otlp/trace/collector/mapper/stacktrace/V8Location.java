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
 * The V8 source location {@code "<file>:<line>:<col>"} that ends a Node/browser frame. The JVM sniffer
 * uses the same shape to reject V8 frames, whose {@code at ...(...)} outline otherwise looks like a JVM
 * frame: a JVM location never carries a column.
 */
final class V8Location {

    private V8Location() {
    }

    static boolean isLocation(String line, int from, int to) {
        return separator(line, from, to) >= 0;
    }

    /**
     * Recognizes the location in {@code line[from..to)} without a regex. The file part may itself
     * contain ':' (Windows drives, {@code node:internal/...}), so the last two colons, each followed by
     * digits only, are the separators.
     *
     * @return the index of the ':' between file and line, or -1 when the range is not a location
     */
    static int separator(String line, int from, int to) {
        final int column = line.lastIndexOf(':', to - 1);
        if (column <= from || !LineNumbers.isDigits(line, column + 1, to)) {
            return -1;
        }
        final int separator = line.lastIndexOf(':', column - 1);
        // at least one file character must precede the separator
        if (separator <= from || !LineNumbers.isDigits(line, separator + 1, column)) {
            return -1;
        }
        return separator;
    }
}
