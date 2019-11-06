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

/**
 * @author Woonduk Kang(emeroad)
 */
public final class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 4096;
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
        if (inputStream == null) {
            throw new NullPointerException("inputStream");
        }
        if (bufferSize < 0) {
            throw new IllegalArgumentException("negative bufferSize");
        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final byte[] buffer = new byte[bufferSize];
            copy(inputStream, outputStream, buffer);
            outputStream.flush();
            return outputStream.toByteArray();
        } finally {
            if (close) {
                closeQuietly(inputStream);
            }
        }
    }

    public static void copy(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
        int readCount;
        while ((readCount = inputStream.read(buffer, 0, buffer.length)) != EOF) {
            outputStream.write(buffer, 0, readCount);
        }
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
