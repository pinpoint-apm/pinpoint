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

import java.util.List;

/**
 * @author youngjin.kim2
 */
public class ATDSupply {

    private @Nullable List<ActiveThreadDump> threadDumps; // required
    private @Nullable String type; // optional
    private @Nullable String subType; // optional
    private @Nullable String version; // optional

    @Nullable
    public List<ActiveThreadDump> getThreadDumps() {
        return threadDumps;
    }

    public void setThreadDumps(@Nullable List<ActiveThreadDump> threadDumps) {
        this.threadDumps = threadDumps;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public String getSubType() {
        return subType;
    }

    public void setSubType(@Nullable String subType) {
        this.subType = subType;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public void setVersion(@Nullable String version) {
        this.version = version;
    }
}
