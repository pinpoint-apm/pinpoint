/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.realtime.collector.receiver.grpc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPoint;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPointLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class GrpcAgentConnectionRepository implements ClusterPointLocator {

    private final Logger logger = LogManager.getLogger(GrpcAgentConnectionRepository.class);

    private final Multimap<ClusterKey, GrpcAgentConnection> connMap =
            Multimaps.synchronizedMultimap(HashMultimap.create());

    public void add(GrpcAgentConnection conn) {
        this.connMap.put(conn.getClusterKey(), conn);
    }

    public void remove(GrpcAgentConnection conn) {
        this.connMap.remove(conn.getClusterKey(), conn);
    }

    Collection<GrpcAgentConnection> getConnections() {
        return new ArrayList<>(this.connMap.values());
    }

    public GrpcAgentConnection getConnection(ClusterKey key) {
        Collection<GrpcAgentConnection> candidates = this.connMap.get(key);

        final int size = candidates.size();
        if (size == 0) {
            return null;
        }
        if (size > 1) {
            if (logger.isWarnEnabled()) {
                logger.warn("Duplicated cluster key detected: key = {}, num = {}", key, size);
            }
        }

        synchronized (this.connMap) {
            final Iterator<GrpcAgentConnection> iterator = candidates.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return null;
            }
        }
    }

    @Override
    public Collection<ClusterPoint> getClusterPointList() {
        Collection<GrpcAgentConnection> values = this.connMap.values();
        return new ArrayList<>(values);
    }

}
