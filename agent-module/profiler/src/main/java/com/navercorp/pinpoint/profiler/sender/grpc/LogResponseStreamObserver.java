/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class LogResponseStreamObserver<ResT> implements StreamObserver<ResT> {
    private final Logger logger;
    private final String name;
    private final long requestId;

    public LogResponseStreamObserver(Logger logger, String name, long requestId) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.name = Objects.requireNonNull(name, "name");
        this.requestId = requestId;
    }

    @Override
    public void onNext(ResT response) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} Request success. result={}", name, logString(response));
        }
    }


    @Override
    public void onError(Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        Metadata metadata = Status.trailersFromThrowable(throwable);
        if (logger.isInfoEnabled()) {
            logger.info("{} Error. requestId={}, {} {}", name, requestId, status, metadata);
        }
    }

    @Override
    public void onCompleted() {
        if (logger.isDebugEnabled()) {
            logger.debug("{} onCompleted. requestId={}", name, requestId);
        }
    }


    private String logString(Object message) {
        if (message == null) {
            return "NULL";
        }
        if (message instanceof GeneratedMessageV3) {
            GeneratedMessageV3 messageV3 = (GeneratedMessageV3) message;
            return TextFormat.shortDebugString(messageV3);
        }
        return message.toString();
    }

    @Override
    public String toString() {
        return "LogResponseStreamObserver{" +
                "logger=" + logger +
                ", name='" + name + '\'' +
                '}';
    }
}
