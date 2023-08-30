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
package com.navercorp.pinpoint.log.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.log.vo.Log;

import java.util.List;

/**
 * @author youngjin.kim2
 */
public class LiveTailBatch {

    private final String fileKey;
    private final List<Log> logs;

    public LiveTailBatch(String fileKey, List<Log> logs) {
        this.fileKey = fileKey;
        this.logs = logs;
    }

    @JsonProperty("fileKey")
    public String getFileKey() {
        return fileKey;
    }

    @JsonProperty("logs")
    public List<Log> getLogs() {
        return logs;
    }

}
