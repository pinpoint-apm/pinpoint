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
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventMarker;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AgentEventMarkerSerializerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void serializeTest() throws Exception {
        AgentEventMarker marker = makeData();
        String jsonValue = mapper.writeValueAsString(marker);
        Map map = mapper.readValue(jsonValue, Map.class);
        Assert.assertEquals(27, map.get("totalCount"));
        logger.debug(map.get("typeCounts").toString());
    }

    private AgentEventMarker makeData() {
        AgentEventMarker marker = new AgentEventMarker();
        for (int i = 0; i < 3; i++) {
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.AGENT_CONNECTED)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.AGENT_PING)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.AGENT_SHUTDOWN)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.AGENT_CLOSED_BY_SERVER)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.AGENT_DEADLOCK_DETECTED)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.USER_THREAD_DUMP)));
            marker.addAgentEvent(new AgentEvent(new AgentEventBo("agent", 1000L, 1001L, AgentEventType.OTHER)));
        }

        return marker;
    }
}
