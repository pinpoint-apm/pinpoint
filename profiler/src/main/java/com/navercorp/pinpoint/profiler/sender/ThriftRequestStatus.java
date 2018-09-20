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
import org.apache.thrift.TBase;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThriftRequestStatus implements RequestStatus<TBase<?, ?>> {

    private final TBase<?, ?> message;
    private final int retryCount;
    private final FutureListener futureListener;


    public ThriftRequestStatus(TBase<?, ?> message, int retryCount) {
        this(message, retryCount, null);
    }

    public ThriftRequestStatus(TBase<?, ?> message, FutureListener futureListener) {
        this(message, 3, futureListener);
    }

    private ThriftRequestStatus(TBase<?, ?> message, int retryCount, FutureListener futureListener) {
        this.message = message;
        this.retryCount = retryCount;
        this.futureListener = futureListener;
    }

    @Override
    public TBase<?, ?> getMessage() {
        return message;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public FutureListener getFutureListener() {
        return futureListener;
    }
}
