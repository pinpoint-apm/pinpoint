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
package com.navercorp.pinpoint.realtime.dto;


import jakarta.annotation.Nullable;

/**
 * @author youngjin.kim2
 */
public class MonitorInfo {

    private int stackDepth; // required
    private @Nullable String stackFrame; // required

    public int getStackDepth() {
        return stackDepth;
    }

    public void setStackDepth(int stackDepth) {
        this.stackDepth = stackDepth;
    }

    @Nullable
    public String getStackFrame() {
        return stackFrame;
    }

    public void setStackFrame(@Nullable String stackFrame) {
        this.stackFrame = stackFrame;
    }
}
