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
package com.navercorp.pinpoint.log.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author youngjin.kim2
 */
public class Log {

    private final long seq;
    private final long timestamp;
    private final String log;

    public Log(long seq, long timestamp, String log) {
        this.seq = seq;
        this.timestamp = timestamp;
        this.log = log;
    }

    @JsonProperty("seq")
    public long getSeq() {
        return seq;
    }

    @JsonProperty("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @JsonProperty("log")
    public String getLog() {
        return log;
    }

}
