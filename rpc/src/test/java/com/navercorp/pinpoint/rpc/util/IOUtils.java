/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.util;

import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Taejin Koo
 */
public final class IOUtils {

    public static byte[] read(final InputStream inputStream) throws IOException {
        return read(inputStream, 100, 1000);
    }

    public static byte[] read(final InputStream inputStream, long waitUnitTime, long maxWaitTime) throws IOException {
        boolean isReceived = TestAwaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                try {
                    int availableSize = inputStream.available();
                    return availableSize > 0;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }, waitUnitTime, maxWaitTime);

        if (!isReceived) {
            throw new IOException("no available data");
        }

        int availableSize = inputStream.available();
        byte[] payload = new byte[availableSize];
        inputStream.read(payload);
        return payload;
    }

    public static void write(OutputStream outputStream, byte[] payload) throws IOException {
        if (outputStream != null) {
            outputStream.write(payload);
            outputStream.flush();
        }
    }

    public static void close(Socket socket) throws IOException {
        com.navercorp.pinpoint.common.util.IOUtils.close(socket);
    }

    public static void closeQuietly(Socket socket) {
        com.navercorp.pinpoint.common.util.IOUtils.closeQuietly(socket);
    }

}
