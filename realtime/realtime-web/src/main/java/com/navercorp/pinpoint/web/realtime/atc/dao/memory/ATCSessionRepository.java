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
package com.navercorp.pinpoint.web.realtime.atc.dao.memory;

import com.navercorp.pinpoint.web.realtime.atc.dto.ATCSession;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author youngjin.kim2
 */
public class ATCSessionRepository {

    private final List<ATCSession> sessions = new CopyOnWriteArrayList<>();

    public void add(WebSocketSession webSocketSession) {
        add(ATCSession.of(webSocketSession));
    }

    private void add(ATCSession session) {
        sessions.add(session);
    }

    void remove(ATCSession session) {
        sessions.remove(session);
    }

    public List<ATCSession> getSessions() {
        return this.sessions;
    }

    public Set<String> getAllDemandedApplicationNames() {
        final List<String> applicationNames = new ArrayList<>(sessions.size());
        for (final ATCSession session: sessions) {
            final String applicationName = session.getDemandApplicationName();
            if (applicationName != null) {
                applicationNames.add(applicationName);
            }
        }
        return new HashSet<>(applicationNames);
    }

    public void remove(WebSocketSession webSocketSession) {
        final ATCSession session = get(webSocketSession);
        if (session == null) {
            return;
        }
        remove(session);
    }

    public ATCSession get(WebSocketSession webSocketSession) {
        for (final ATCSession session: sessions) {
            if (session.getWebSocketSession() == webSocketSession) {
                return session;
            }
        }
        return null;
    }

}
