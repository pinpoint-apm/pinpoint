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
package com.navercorp.pinpoint.realtime.dto.mapper;

import com.navercorp.pinpoint.realtime.dto.ActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;

/**
 * @author youngjin.kim2
 */
@SuppressWarnings("DuplicatedCode")
public class ActiveThreadDumpMapper {

    public static ActiveThreadDump fromThrift(TActiveThreadDump s) {
        final ActiveThreadDump t = new ActiveThreadDump();
        t.setStartTime(s.getStartTime());
        t.setLocalTraceId(s.getLocalTraceId());
        t.setSampled(s.isSampled());
        t.setTransactionId(s.getTransactionId());
        t.setEntryPoint(s.getEntryPoint());
        t.setThreadDump(ThreadDumpMapper.fromThrift(s.getThreadDump()));
        return t;
    }

    public static ActiveThreadDump fromThrift(TActiveThreadLightDump s) {
        final ActiveThreadDump t = new ActiveThreadDump();
        t.setStartTime(s.getStartTime());
        t.setLocalTraceId(s.getLocalTraceId());
        t.setSampled(s.isSampled());
        t.setTransactionId(s.getTransactionId());
        t.setEntryPoint(s.getEntryPoint());
        t.setThreadDump(ThreadDumpMapper.fromThrift(s.getThreadDump()));
        return t;
    }

    public static TActiveThreadDump toThriftDetailed(ActiveThreadDump s) {
        if (s == null) {
            return null;
        }
        final TActiveThreadDump t = new TActiveThreadDump();
        t.setStartTime(s.getStartTime());
        t.setLocalTraceId(s.getLocalTraceId());
        t.setSampled(s.isSampled());
        t.setTransactionId(s.getTransactionId());
        t.setEntryPoint(s.getEntryPoint());
        t.setThreadDump(ThreadDumpMapper.toThriftDetailed(s.getThreadDump()));
        return t;
    }

    public static TActiveThreadLightDump toThriftLight(ActiveThreadDump s) {
        if (s == null) {
            return null;
        }
        final TActiveThreadLightDump t = new TActiveThreadLightDump();
        t.setStartTime(s.getStartTime());
        t.setLocalTraceId(s.getLocalTraceId());
        t.setSampled(s.isSampled());
        t.setTransactionId(s.getTransactionId());
        t.setEntryPoint(s.getEntryPoint());
        t.setThreadDump(ThreadDumpMapper.toThriftLight(s.getThreadDump()));
        return t;
    }

}
