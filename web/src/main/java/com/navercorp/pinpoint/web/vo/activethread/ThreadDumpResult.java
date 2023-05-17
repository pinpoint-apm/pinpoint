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
package com.navercorp.pinpoint.web.vo.activethread;

/**
 * @author youngjin.kim2
 */
public class ThreadDumpResult {
    private final AgentActiveThreadDumpList threadDumpData;
    private final String type;
    private final String subType;
    private final String version;

    public ThreadDumpResult(AgentActiveThreadDumpList threadDumpData, String type, String subType, String version) {
        this.threadDumpData = threadDumpData;
        this.type = type;
        this.subType = subType;
        this.version = version;
    }

    public AgentActiveThreadDumpList getThreadDumpData() {
        return threadDumpData;
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    public String getVersion() {
        return version;
    }
}
