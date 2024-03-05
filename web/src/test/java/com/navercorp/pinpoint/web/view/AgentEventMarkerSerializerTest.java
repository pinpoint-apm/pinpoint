/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventMarker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class AgentEventMarkerSerializerTest {
    private final ObjectMapper mapper = Jackson.newMapper();
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void serializeTest() throws Exception {
        AgentEventMarker marker = makeData();
        String jsonValue = mapper.writeValueAsString(marker);
        Map<String, Object> map = mapper.readValue(jsonValue, TypeRef.map());
        Assertions.assertEquals(27, map.get("totalCount"));
        logger.debug(map.get("typeCounts").toString());
    }

    private AgentEventMarker makeData() {
        AgentEventMarker marker = new AgentEventMarker();
        for (int i = 0; i < 3; i++) {
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.AGENT_CONNECTED));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.AGENT_PING));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.AGENT_SHUTDOWN));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.AGENT_CLOSED_BY_SERVER));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.AGENT_DEADLOCK_DETECTED));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.USER_THREAD_DUMP));
            marker.addAgentEvent(new AgentEvent("agent", 1001L, 1000L, AgentEventType.OTHER));
        }

        return marker;
    }
}
