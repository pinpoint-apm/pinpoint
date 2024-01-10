/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.it.plugin.utils;

import com.navercorp.pinpoint.common.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StdoutRecorder {

    public String record(Runnable runnable) {
        try {
            return record0(runnable);
        } catch (UnsupportedEncodingException e) {
            // UTF-8 is actually guaranteed to be supported by the JVM
            throw new RuntimeException("Unsupported charset: utf-8", e);
        }
    }

    public String record0(Runnable runnable) throws UnsupportedEncodingException {
        Objects.requireNonNull(runnable, "runnable");

        final PrintStream originalOut = System.out;

        final StringOutputStream stream = new StringOutputStream();
        final PrintStream printStream = new PrintStream(stream, false, StandardCharsets.UTF_8.name());

        System.setOut(printStream);

        try {
            runnable.run();
            return stream.toString();
        } finally {
            System.setOut(originalOut);
            IOUtils.closeQuietly(printStream);
            IOUtils.closeQuietly(stream);
        }
    }

    private static class StringOutputStream extends ByteArrayOutputStream {
        @Override
        public synchronized String toString() {
            return new String(this.buf, 0, this.count, StandardCharsets.UTF_8);
        }
    }

}
