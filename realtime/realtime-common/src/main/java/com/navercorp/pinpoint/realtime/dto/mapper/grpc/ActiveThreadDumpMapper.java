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
        assert s.getThreadDump() != null;
        return PActiveThreadDump.newBuilder()
                .setStartTime(s.getStartTime())
                .setLocalTraceId(s.getLocalTraceId())
                .setThreadDump(ThreadDumpMapper.into(s.getThreadDump()))
                .setSampled(s.isSampled())
                .setTransactionId(s.getTransactionId())
                .setEntryPoint(s.getEntryPoint())
                .build();
    }

    static PActiveThreadLightDump intoLight(ActiveThreadDump s) {
        assert s.getThreadDump() != null;
        return PActiveThreadLightDump.newBuilder()
                .setStartTime(s.getStartTime())
                .setLocalTraceId(s.getLocalTraceId())
                .setThreadDump(ThreadDumpMapper.intoLight(s.getThreadDump()))
                .setSampled(s.isSampled())
                .setTransactionId(s.getTransactionId())
                .setEntryPoint(s.getEntryPoint())
                .build();
    }

}
