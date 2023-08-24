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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author youngjin.kim2
 */
public class LogHost {

    private final String name;
    private final List<String> files;

    public LogHost(String name, List<String> files) {
        this.name = name;
        this.files = files;
    }

    public static List<LogHost> from(List<FileKey> fileKey) {
        final Map<String, List<String>> byName = new HashMap<>();
        for (FileKey key : fileKey) {
            final String hostName = key.getHostKey().getHostName();
            final List<String> keys = byName.computeIfAbsent(hostName, k -> new ArrayList<>());
            keys.add(key.getFileName());
        }

        final List<LogHost> hosts = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry: byName.entrySet()) {
            hosts.add(new LogHost(entry.getKey(), entry.getValue()));
        }
        return hosts;
    }

    public String getName() {
        return name;
    }

    public List<String> getFiles() {
        return files;
    }
}
