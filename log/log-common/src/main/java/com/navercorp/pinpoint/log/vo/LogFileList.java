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

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class LogFileList {

    private List<String> fileKeys;

    public LogFileList() {}

    public LogFileList(List<FileKey> fileKeys) {
        Objects.requireNonNull(fileKeys, "fileKeys");
        this.fileKeys = fileKeys.stream().map(FileKey::toString).distinct().toList();
    }

    public static LogFileList of(List<FileKey> fileKeys) {
        return new LogFileList(fileKeys);
    }

    public List<String> getFileKeys() {
        return fileKeys;
    }

    public void setFileKeys(List<String> fileKeys) {
        this.fileKeys = fileKeys;
    }

}
