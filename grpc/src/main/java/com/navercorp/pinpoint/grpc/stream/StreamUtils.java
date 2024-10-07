/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.stream;

import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;


/**
 * @author Woonduk Kang(emeroad)
 */
public final class StreamUtils {

    private StreamUtils() {
    }

    public static void onCompleted(final StreamObserver<?> streamObserver) {
        onCompleted(streamObserver, null);
    }

    public static void onCompleted(final StreamObserver<?> streamObserver, Consumer<Throwable> consumer) {
        if (streamObserver != null) {
            try {
                streamObserver.onCompleted();
            } catch (Throwable th) {
                 if (consumer != null) {
                    consumer.accept(th);
                }
            }
        }
    }

    public static void onError(final StreamObserver<?> streamObserver, Throwable t) {
        onError(streamObserver, t, null);
    }

    public static void onError(final StreamObserver<?> streamObserver, Throwable t, Consumer<Throwable> consumer) {
        if (streamObserver != null) {
            try {
                streamObserver.onError(t);
            } catch (Throwable th) {
                if (consumer != null) {
                    consumer.accept(th);
                }
            }

        }
    }
}
