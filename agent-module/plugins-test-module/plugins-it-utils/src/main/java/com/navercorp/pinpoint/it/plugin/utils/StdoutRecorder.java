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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StdoutRecorder {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

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

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(stream, false, UTF8.name());

        System.setOut(printStream);

        try {
            runnable.run();
            return stream.toString(UTF8.name());
        } finally {
            System.setOut(originalOut);
            IOUtils.closeQuietly(printStream);
            IOUtils.closeQuietly(stream);
        }
    }

}
