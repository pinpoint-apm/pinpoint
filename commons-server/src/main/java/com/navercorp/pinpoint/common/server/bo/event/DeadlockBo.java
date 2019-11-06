/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.event;

import java.util.List;

/**
 * @author jaehong.kim
 */
public class DeadlockBo {
    public static final int UNCOLLECTED_INT_VALUE = -1;

    private int deadlockedThreadCount = UNCOLLECTED_INT_VALUE;
    private List<ThreadDumpBo> threadDumpBoList;

    public int getDeadlockedThreadCount() {
        return deadlockedThreadCount;
    }

    public void setDeadlockedThreadCount(int deadlockedThreadCount) {
        this.deadlockedThreadCount = deadlockedThreadCount;
    }

    public List<ThreadDumpBo> getThreadDumpBoList() {
        return threadDumpBoList;
    }

    public void setThreadDumpBoList(List<ThreadDumpBo> threadDumpBoList) {
        this.threadDumpBoList = threadDumpBoList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeadlockBo{");
        sb.append("deadlockedThreadCount=").append(deadlockedThreadCount);
        sb.append(", threadDumpBoList=").append(threadDumpBoList);
        sb.append('}');
        return sb.toString();
    }
}
