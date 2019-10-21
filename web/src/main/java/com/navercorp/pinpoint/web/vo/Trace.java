/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import java.util.Objects;

/**
 * @author emeroad
 */
public class Trace {

    private final String transactionId;
    private final long executionTime;
    private final long startTime;

    private final int exceptionCode;

    public Trace(String transactionId, long executionTime, long startTime, int exceptionCode) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.executionTime = executionTime;
        this.startTime = startTime;
        this.exceptionCode = exceptionCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }
}
