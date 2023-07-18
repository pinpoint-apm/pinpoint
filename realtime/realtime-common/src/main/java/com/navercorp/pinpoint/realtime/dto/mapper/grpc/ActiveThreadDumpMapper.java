/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.dto.mapper.grpc;

import com.navercorp.pinpoint.grpc.trace.PActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PActiveThreadLightDump;
import com.navercorp.pinpoint.realtime.dto.ActiveThreadDump;

/**
 * @author youngjin.kim2
 */
class ActiveThreadDumpMapper {

    static ActiveThreadDump from(PActiveThreadDump s) {
        final ActiveThreadDump t = new ActiveThreadDump();
        t.setStartTime(s.getStartTime());
        t.setLocalTraceId(s.getLocalTraceId());
        t.setThreadDump(ThreadDumpMapper.from(s.getThreadDump()));
        t.setSampled(s.getSampled());
        t.setTransactionId(s.getTransactionId());
        t.setEntryPoint(s.getEntryPoint());
        return t;
    }

    static ActiveThreadDump fromLight(PActiveThreadLightDump s) {
        final ActiveThreadDump t = new ActiveThreadDump();
        t.setStartTime(s.getStartTime());
        t.setLocalTraceId(s.getLocalTraceId());
        t.setThreadDump(ThreadDumpMapper.from(s.getThreadDump()));
        t.setSampled(s.getSampled());
        t.setTransactionId(s.getTransactionId());
        t.setEntryPoint(s.getEntryPoint());
        return t;
    }

    static PActiveThreadDump into(ActiveThreadDump s) {
        final PActiveThreadDump.Builder builder = PActiveThreadDump.newBuilder()
                .setStartTime(s.getStartTime())
                .setLocalTraceId(s.getLocalTraceId())
                .setSampled(s.isSampled());

        if (s.getThreadDump() != null) {
            builder.setThreadDump(ThreadDumpMapper.into(s.getThreadDump()));
        }
        if (s.getTransactionId() != null) {
            builder.setTransactionId(s.getTransactionId());
        }
        if (s.getEntryPoint() != null) {
            builder.setEntryPoint(s.getEntryPoint());
        }

        return builder.build();
    }

    static PActiveThreadLightDump intoLight(ActiveThreadDump s) {
        PActiveThreadLightDump.Builder builder = PActiveThreadLightDump.newBuilder()
                .setStartTime(s.getStartTime())
                .setLocalTraceId(s.getLocalTraceId())
                .setSampled(s.isSampled());
        if (s.getTransactionId() != null) {
            builder.setTransactionId(s.getTransactionId());
        }
        if (s.getEntryPoint() != null) {
            builder.setEntryPoint(s.getEntryPoint());
        }
        if (s.getThreadDump() != null) {
            builder.setThreadDump(ThreadDumpMapper.intoLight(s.getThreadDump()));
        }
        return builder.build();
    }

}
