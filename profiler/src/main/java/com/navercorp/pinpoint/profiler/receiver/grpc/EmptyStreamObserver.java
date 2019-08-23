/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taejin Koo
 */
public class EmptyStreamObserver implements StreamObserver<Empty> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onNext(Empty value) {
        logger.info("onNext. message:{}", value);
    }

    @Override
    public void onError(Throwable t) {
        logger.info("onError. message:{}", t.getMessage(), t);
    }

    @Override
    public void onCompleted() {
        logger.info("onCompleted.");
    }

    static StreamObserver create() {
        return new EmptyStreamObserver();
    }

}
