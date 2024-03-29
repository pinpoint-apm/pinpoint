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
public record LiveTailBatch(String fileKey, List<Log> logs) {

    @Override
    @JsonProperty("fileKey")
    public String fileKey() {
        return fileKey;
    }

    @Override
    @JsonProperty("logs")
    public List<Log> logs() {
        return logs;
    }

}
