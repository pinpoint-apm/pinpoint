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
package com.navercorp.pinpoint.log.dto;

import com.navercorp.pinpoint.log.vo.FileKey;

/**
 * @author youngjin.kim2
 */
public class LogDemand {

    private FileKey fileKey;
    private long durationMillis;

    public LogDemand() {}

    public LogDemand(FileKey fileKey, long durationMillis) {
        this.fileKey = fileKey;
        this.durationMillis = durationMillis;
    }

    public FileKey getFileKey() {
        return fileKey;
    }

    public void setFileKey(FileKey fileKey) {
        this.fileKey = fileKey;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

}
