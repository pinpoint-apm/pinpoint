/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int MAX_BUFFER_SIZE = 1024 * 1024;

    public static final int EOF = -1;

    private IOUtils() {
    }

    public static byte[] toByteArray(final InputStream inputStream) throws IOException {
        return toByteArray(inputStream, DEFAULT_BUFFER_SIZE, true);
    }

    public static byte[] toByteArray(final InputStream inputStream, boolean close) throws IOException {
        return toByteArray(inputStream, DEFAULT_BUFFER_SIZE, close);
    }

    public static byte[] toByteArray(final InputStream inputStream, int bufferSize, boolean close) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        if (bufferSize < 0) {
            throw new IllegalArgumentException("negative bufferSize");
        }

        try {
            bufferSize = calculateBufferSize(inputStream, bufferSize);
            final byte[] buffer = new byte[bufferSize];

            final int readBytes = readFully(inputStream, buffer);
            if (readBytes < buffer.length) {
                // EOF before the buffer was full: available() overestimated the size.
                return Arrays.copyOf(buffer, readBytes);
            }

            // The buffer is full. Probe a single byte to tell "exactly full" from "more to come"
            // without going through a ByteArrayOutputStream: available() is exact for jar and file streams,
            // so this is the common path.
            final int next = inputStream.read();
            if (next == EOF) {
                return buffer;
            }

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(buffer.length * 2);
            outputStream.write(buffer, 0, buffer.length);
            outputStream.write(next);

            copy(inputStream, outputStream, buffer);

            return outputStream.toByteArray();
        } finally {
            if (close) {
                closeQuietly(inputStream);
            }
        }
    }

    /**
     * Fills the buffer until it is full or the stream reaches EOF.
     *
     * @return the number of bytes read, less than the buffer length only when EOF was reached first.
     */
    private static int readFully(final InputStream inputStream, final byte[] buffer) throws IOException {
        final int bufferLength = buffer.length;
        int offset = 0;
        while (offset < bufferLength) {
            // OS buffer optimization: a single read may return fewer bytes than requested
            final int readBytes = inputStream.read(buffer, offset, bufferLength - offset);
            if (readBytes == EOF) {
                break;
            }
            offset += readBytes;
        }
        return offset;
    }

    private static int calculateBufferSize(final InputStream inputStream, int defaultBufferSize) throws IOException {
        final int expectedLength = inputStream.available();
        // available() is 0 or 1 when the stream cannot estimate its size; the smallest class file javac emits is 83 bytes.
        if (expectedLength < 64) {
            return defaultBufferSize;
        }
        return Math.min(expectedLength, MAX_BUFFER_SIZE);
    }

    public static void copy(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != EOF) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }


    public static void closeQuietly(Closeable closeable) {
        closeQuietly(closeable, null);
    }

    public static void closeQuietly(Closeable closeable, Consumer<IOException> consumer) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                if (consumer != null) {
                    consumer.accept(ioe);
                }
            }
        }
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }


}
