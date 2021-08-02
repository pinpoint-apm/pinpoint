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
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Objects;

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
        // https://gitlab.ow2.org/asm/asm/-/blob/master/asm/src/main/java/org/objectweb/asm/ClassReader.java#L316
        // memory allocation optimization
        bufferSize = calculateBufferSize(inputStream, bufferSize);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final byte[] buffer = new byte[bufferSize];
//            copy(inputStream, outputStream, buffer);
            final int readCount = copy0(inputStream, outputStream, buffer);
            if (readCount == 1 && outputStream.size() == buffer.length) {
                return buffer;
            }
            outputStream.flush();
            return outputStream.toByteArray();
        } finally {
            if (close) {
                closeQuietly(inputStream);
            }
        }
    }

    private static int calculateBufferSize(final InputStream inputStream, int defaultBufferSize) throws IOException {
        final int expectedLength = inputStream.available();
        if (expectedLength < 256) {
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

    private static int copy0(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
        int readCount = 0;
        int bytesRead;
        while ((bytesRead  = inputStream.read(buffer, 0, buffer.length)) != EOF) {
            outputStream.write(buffer, 0, bytesRead);
            readCount++;
        }
        return readCount;
    }


    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
                // skip
            }
        }
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
                // skip
            }
        }
    }

    public static void close(Socket socket) throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    public static void closeQuietly(DatagramSocket datagramSocket) {
        if (datagramSocket != null) {
            datagramSocket.close();
        }
    }


}
