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

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.Matchers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public final class AwaitIOUtils {

    public static byte[] read(final InputStream inputStream) throws IOException {
        return read(inputStream, 100, 1000);
    }

    public static byte[] read(final InputStream inputStream, long waitUnitTime, long maxWaitTime) throws IOException {
        try {
            waitForIoReady(inputStream, waitUnitTime, maxWaitTime);
        } catch (ConditionTimeoutException e) {
            throw new IOException("no available data", e);
        }

        int availableSize = inputStream.available();
        byte[] payload = new byte[availableSize];
        inputStream.read(payload);
        return payload;
    }

    private static void waitForIoReady(final InputStream inputStream, long waitUnitTime, long maxWaitTime) {
        Awaitility.await()
                .pollDelay(waitUnitTime, TimeUnit.MILLISECONDS)
                .timeout(maxWaitTime, TimeUnit.MILLISECONDS)
                .until(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return inputStream.available();
                    }
                }, Matchers.greaterThan(0));
    }

    public static void write(OutputStream outputStream, byte[] payload) throws IOException {
        if (outputStream != null) {
            outputStream.write(payload);
            outputStream.flush();
        }
    }



}
