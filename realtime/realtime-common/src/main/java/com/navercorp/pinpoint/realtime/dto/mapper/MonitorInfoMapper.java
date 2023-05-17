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

import com.navercorp.pinpoint.realtime.dto.MonitorInfo;
import com.navercorp.pinpoint.thrift.dto.command.TMonitorInfo;

/**
 * @author youngjin.kim2
 */
public class MonitorInfoMapper {

    public static MonitorInfo fromThrift(TMonitorInfo s) {
        final MonitorInfo t = new MonitorInfo();
        t.setStackDepth(s.getStackDepth());
        t.setStackFrame(s.getStackFrame());
        return t;
    }

    public static TMonitorInfo toThrift(MonitorInfo s) {
        final TMonitorInfo t = new TMonitorInfo();
        t.setStackDepth(s.getStackDepth());
        t.setStackFrame(s.getStackFrame());
        return t;
    }

}
