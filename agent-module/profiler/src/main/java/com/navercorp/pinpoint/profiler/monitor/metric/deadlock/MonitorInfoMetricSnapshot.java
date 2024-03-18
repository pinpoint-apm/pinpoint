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

package com.navercorp.pinpoint.profiler.monitor.metric.deadlock;

/**
 * @author jaehong.kim
 */
public class MonitorInfoMetricSnapshot {
    private int stackDepth;
    private String stackFrame;

    public int getStackDepth() {
        return stackDepth;
    }

    public void setStackDepth(int stackDepth) {
        this.stackDepth = stackDepth;
    }

    public String getStackFrame() {
        return stackFrame;
    }

    public void setStackFrame(String stackFrame) {
        this.stackFrame = stackFrame;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MonitorInfoMetricSnapshot{");
        sb.append("stackDepth=").append(stackDepth);
        sb.append(", stackFrame='").append(stackFrame).append('\'');
        sb.append('}');
        return sb.toString();
    }
}