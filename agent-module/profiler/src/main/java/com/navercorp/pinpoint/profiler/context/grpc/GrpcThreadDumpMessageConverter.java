/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.trace.PThreadDump;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ThreadDumpMapper;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.ThreadDumpMetricSnapshot;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class GrpcThreadDumpMessageConverter implements MessageConverter<Object, PThreadDump> {
    private final ThreadDumpMapper mapper;

    public GrpcThreadDumpMessageConverter(ThreadDumpMapper threadDumpMapper) {
        this.mapper = Objects.requireNonNull(threadDumpMapper, "threadDumpMapper");
    }

    @Override
    public PThreadDump toMessage(Object message) {
        if (message instanceof ThreadDumpMetricSnapshot) {
            final ThreadDumpMetricSnapshot threadDumpMetricSnapshot = (ThreadDumpMetricSnapshot) message;
            return mapper.map(threadDumpMetricSnapshot);
        } else {
            return null;
        }
    }
}