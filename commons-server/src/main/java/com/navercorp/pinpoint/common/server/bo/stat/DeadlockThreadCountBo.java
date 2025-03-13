/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.stat;

/**
 * @author Taejin Koo
 * @author jaehong.kim - Remove TThreadDump List
 */
public class DeadlockThreadCountBo extends AgentStatDataBasePoint implements AgentWarningStatDataPoint {

    public static final int UNCOLLECTED_INT_VALUE = -1;

    private int deadlockedThreadCount = UNCOLLECTED_INT_VALUE;


    @Override
    public AgentStatType getAgentStatType() {
        return AgentStatType.DEADLOCK;
    }

    public int getDeadlockedThreadCount() {
        return deadlockedThreadCount;
    }

    public void setDeadlockedThreadCount(int deadlockedThreadCount) {
        this.deadlockedThreadCount = deadlockedThreadCount;
    }

    @Override
    public String toString() {
        return "DeadlockThreadCountBo{" +
                "agentId='" + agentId + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", timestamp=" + timestamp +
                ", deadlockedThreadCount=" + deadlockedThreadCount +
                '}';
    }
}
