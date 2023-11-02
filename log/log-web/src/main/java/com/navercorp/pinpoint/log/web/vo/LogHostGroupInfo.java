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

import com.navercorp.pinpoint.log.vo.FileKey;

import java.util.ArrayList;
import java.util.List;

/**
 * @author youngjin.kim2
 */
public record LogHostGroupInfo(List<String> hosts, List<String> files) {

    public static LogHostGroupInfo compose(List<FileKey> fileKeys) {
        List<String> hosts = new ArrayList<>(fileKeys.size());
        List<String> files = new ArrayList<>(fileKeys.size());
        for (FileKey fileKey: fileKeys) {
            hosts.add(fileKey.getHostKey().getHostName());
            files.add(fileKey.getFileName());
        }
        return new LogHostGroupInfo(
                hosts.stream().distinct().toList(),
                files.stream().distinct().toList()
        );
    }

}
