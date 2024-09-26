/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.realtime.collector.sink;

import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import reactor.core.publisher.MonoSink;

import java.util.Objects;

public class ActiveThreadDumpPublisher implements Publisher<PCmdActiveThreadDumpRes> {

    private final MonoSink<PCmdActiveThreadDumpRes> sink;

    public ActiveThreadDumpPublisher(MonoSink<PCmdActiveThreadDumpRes> sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
    }

    @Override
    public void publish(PCmdActiveThreadDumpRes response) {
        this.sink.success(response);
    }

    @Override
    public void error(Throwable throwable) {
        this.sink.error(throwable);
    }
}