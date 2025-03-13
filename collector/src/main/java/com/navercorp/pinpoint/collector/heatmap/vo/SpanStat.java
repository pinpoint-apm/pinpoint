/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.vo;

import java.util.Objects;

/**
 * @author minwoo-jung
 */
public class SpanStat {

    private final String applicationName;
    private final String agentId;
    private final long startTime;
    private final int elapsed;

    private final Boolean isSuccess;

    public SpanStat(String applicationName, String agentId, long startTime, int elapsed, int errCode) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.startTime = startTime;
        this.elapsed = elapsed;
        this.isSuccess = errCode == 0;
    }
    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getElapsed() {
        return elapsed;
    }

    public Boolean isSuccess() {
        return isSuccess;
    }

}
