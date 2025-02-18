/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPingSessionRegistry implements PingSessionRegistry {

    private final ListMultimap<Long, PingSession> map;
    private final int limit;

    public DefaultPingSessionRegistry() {
        this(3);
    }


    public DefaultPingSessionRegistry(int limit) {
        ListMultimap<Long, PingSession> multimap = LinkedListMultimap.create();
        this.map = Multimaps.synchronizedListMultimap(multimap);
        this.limit = limit;
    }

    @Override
    public boolean add(PingSession pingSession) {
        Objects.requireNonNull(pingSession, "pingSession");

        final List<PingSession> pingSessions = map.get(pingSession.getTransportId());
        synchronized (map) {
            if (pingSessions.size() >= limit) {
                // OOM defense
                pingSessions.remove(0);
            }
            pingSessions.add(pingSession);
        }
        return true;
    }

    @Override
    public PingSession get(Long transportId) {
        Objects.requireNonNull(transportId, "transportId");

        final List<PingSession> pingSessions = map.get(transportId);
        synchronized (map) {
            final int size = pingSessions.size();
            if (size == 0) {
                return null;
            }
            return pingSessions.get(size - 1);
        }
    }

    @Override
    public boolean remove(PingSession pingSession) {
        Objects.requireNonNull(pingSession, "pingSession");

        return map.remove(pingSession.getTransportId(), pingSession);
    }

    @Override
    public List<PingSession> values() {
        List<PingSession> list = new ArrayList<>(32);
        Set<Long> keys = this.map.keySet();
        synchronized (this.map) {
            for (Long key : keys) {
                List<PingSession> pingSessions = this.map.get(key);
                final int size = pingSessions.size();
                if (size != 0) {
                    list.add(pingSessions.get(size - 1));
                }
            }
            return list;
        }
    }

    @Override
    public int size() {
        return map.size();
    }
}
