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
package com.navercorp.pinpoint.web.realtime.atc.service;

import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import com.navercorp.pinpoint.web.realtime.atc.dto.ATCSession;
import com.navercorp.pinpoint.web.realtime.atc.service.DemandPublishService;
import com.navercorp.pinpoint.web.realtime.atc.service.DemandRegisterService;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class DemandRegisterServiceImpl implements DemandRegisterService {

    private final ATCSessionRepository sessionRepository;
    private final DemandPublishService demandPublishService;

    public DemandRegisterServiceImpl(
            ATCSessionRepository sessionRepository,
            DemandPublishService demandPublishService
    ) {
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository");
        this.demandPublishService = Objects.requireNonNull(demandPublishService, "demandPublishService");
    }

    @Override
    public void registerSession(WebSocketSession session) {
        this.sessionRepository.add(session);
    }

    @Override
    public void unregisterSession(WebSocketSession session) {
        this.sessionRepository.remove(session);
    }

    @Override
    public void registerDemandToSession(WebSocketSession webSession, String applicationName) {
        final ATCSession session = this.sessionRepository.get(webSession);
        if (session != null) {
            session.setDemand(applicationName);
            this.demandPublishService.demand(applicationName);
        }
    }
}
