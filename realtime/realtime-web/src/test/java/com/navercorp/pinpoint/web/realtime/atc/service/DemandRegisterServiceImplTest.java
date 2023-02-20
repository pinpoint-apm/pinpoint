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
import com.navercorp.pinpoint.web.realtime.atc.service.DemandRegisterServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class DemandRegisterServiceImplTest {

    @Mock DemandPublishService demandPublishService;

    @Test
    public void testRegisterDemand() {
        final ATCSessionRepository sessionRepository = new ATCSessionRepository();
        final DemandRegisterServiceImpl service = new DemandRegisterServiceImpl(
                sessionRepository, demandPublishService);

        final WebSocketSession session = Mockito.mock(WebSocketSession.class);
        final String applicationName = "test-application";

        service.registerSession(session);
        service.registerDemandToSession(session, applicationName);

        final ATCSession atcSession = sessionRepository.get(session);
        assertThat(atcSession.getDemandApplicationName()).isEqualTo(applicationName);
        assertThat(atcSession.getWebSocketSession()).isEqualTo(session);

        assertThat(sessionRepository.getAllDemandedApplicationNames()).containsExactly(applicationName);
    }

}
