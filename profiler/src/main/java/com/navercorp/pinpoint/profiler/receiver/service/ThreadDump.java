/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;

import java.lang.management.ThreadInfo;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThreadDump {

    private final ActiveTraceSnapshot activeTraceSnapshot;
    private final ThreadInfo threadInfo;

    public ThreadDump(ActiveTraceSnapshot activeTraceSnapshot, ThreadInfo threadInfo) {
        this.activeTraceSnapshot = Assert.requireNonNull(activeTraceSnapshot, "activeTraceSnapshot");
        this.threadInfo = Assert.requireNonNull(threadInfo, "threadInfo");
    }

    public ActiveTraceSnapshot getActiveTraceSnapshot() {
        return activeTraceSnapshot;
    }

    public ThreadInfo getThreadInfo() {
        return threadInfo;
    }
}
