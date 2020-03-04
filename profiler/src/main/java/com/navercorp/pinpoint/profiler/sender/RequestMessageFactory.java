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

package com.navercorp.pinpoint.profiler.sender;


import com.navercorp.pinpoint.rpc.FutureListener;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class RequestMessageFactory {

    private RequestMessageFactory() {
    }

    public static <T> RequestMessage<T> request(T message, int retryCount) {
        return new RetryRequestMessage<T>(message, retryCount);
    }

    public static <T> RequestMessage<T> request(T message, FutureListener futureListener) {
        return new ListenerableRequestMessage<T>(message, futureListener);
    }

}
