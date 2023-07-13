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
package com.navercorp.pinpoint.profiler.context.exception.model;

/**
 * @author intr3p1d
 */
public class ExceptionContextValue {

    private static final Throwable INITIAL_EXCEPTION = null;
    private Throwable previous = INITIAL_EXCEPTION;
    private long startTime = 0;

    public Throwable getPrevious() {
        return previous;
    }

    public void setPrevious(Throwable previous) {
        this.previous = previous;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
