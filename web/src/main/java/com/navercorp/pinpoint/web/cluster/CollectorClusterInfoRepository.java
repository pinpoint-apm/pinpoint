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

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author koo.taejin
 *
 */
public class CollectorClusterInfoRepository {

    private final Map<ClusterId, Set<ClusterKey>> repository = new HashMap<>();

    private final Object lock = new Object();

    public void put(ClusterId clusterId, Set<ClusterKey> profilerInfoSet) {
        Objects.requireNonNull(clusterId, "clusterId");
        Objects.requireNonNull(profilerInfoSet, "profilerInfoSet");

        synchronized (lock) {
            repository.put(clusterId, profilerInfoSet);
        }
    }

    public void remove(ClusterId clusterId) {
        Objects.requireNonNull(clusterId, "clusterId");

        synchronized (lock) {
            repository.remove(clusterId);
        }
    }

    public List<ClusterId> get(ClusterKey agentKey) {
        Objects.requireNonNull(agentKey, "agentKey");

        final List<ClusterId> result = new ArrayList<>();
        synchronized (lock) {
            for (Map.Entry<ClusterId, Set<ClusterKey>> entry : repository.entrySet()) {
                final Set<ClusterKey> valueSet = entry.getValue();
                final boolean exist = valueSet.contains(agentKey);
                if (exist) {
                    final ClusterId clusterId = entry.getKey();
                    result.add(clusterId);
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

    @Override
    public String toString() {
        synchronized (lock) {
            return repository.toString();
        }
    }

}
