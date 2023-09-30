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
package com.navercorp.pinpoint.realtime.vo;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;

/**
 * @author youngjin.kim2
 */
public class ProfilerDescription {

    private final ClusterKey clusterKey;

    public ProfilerDescription(ClusterKey clusterKey) {
        this.clusterKey = clusterKey;
    }

    public ClusterKey getClusterKey() {
        return clusterKey;
    }

    @Override
    public String toString() {
        return clusterKey.toString();
    }

    public static ProfilerDescription fromString(String raw) {
        String[] words = raw.split(":", 3);
        if (words.length != 4) {
            throw new RuntimeException("Invalid serialized profiler description: " + raw);
        }
        return new ProfilerDescription(new ClusterKey(words[0], words[1], Long.parseLong(words[2])));
    }

}
