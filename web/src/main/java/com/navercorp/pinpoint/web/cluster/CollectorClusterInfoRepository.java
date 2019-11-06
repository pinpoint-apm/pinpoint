/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.cluster;

import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author koo.taejin
 *
 */
public class CollectorClusterInfoRepository {

    private static final Charset charset = StandardCharsets.UTF_8;

    // for test
    static final String PROFILER_SEPARATOR = "\r\n";

    private final Map<String, Set<String>> repository = new HashMap<>();


    private final Object lock = new Object();

    public void put(String id, byte[] bytes) {

        final Set<String> profilerInfoSet = newProfilerInfo(bytes);
        synchronized (lock) {
            repository.put(id, profilerInfoSet);
        }
    }

    private Set<String> newProfilerInfo(byte[] bytes) {
        if (bytes == null) {
            return Collections.emptySet();
        }

        final String strData = new String(bytes, charset);
        final List<String> profilerInfoList = Arrays.asList(StringUtils.tokenizeToStringArray(strData, PROFILER_SEPARATOR));
        return new HashSet<>(profilerInfoList);
    }

    public void remove(String id) {
        synchronized (lock) {
            repository.remove(id);
        }
    }

    public List<String> get(String applicationName, String agentId, long startTimeStamp) {
        final String key = bindingKey(applicationName, agentId, startTimeStamp);

        final List<String> result = new ArrayList<>();
        synchronized (lock) {
            for (Map.Entry<String, Set<String>> entry : repository.entrySet()) {
                final Set<String> valueSet = entry.getValue();
                final boolean exist = valueSet.contains(key);
                if (exist) {
                    final String id = entry.getKey();
                    result.add(id);
                }
            }
        }

        return result;
    }

    public void clear() {
        synchronized (lock) {
            repository.clear();
        }
    }

    private String bindingKey(String applicationName, String agentId, long startTimeStamp) {
        StringBuilder key = new StringBuilder(64);

        key.append(applicationName);
        key.append(':');
        key.append(agentId);
        key.append(':');
        key.append(startTimeStamp);

        return key.toString();
    }

    @Override
    public String toString() {
        synchronized (lock) {
            return repository.toString();
        }
    }

}
