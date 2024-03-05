package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class AgentEventTest {

    public static final Comparator<AgentEvent> LEGACY_COMPARATOR = (o1, o2) -> {
        int eventTimestampComparison = Long.compare(o1.getEventTimestamp(), o2.getEventTimestamp());
        if (eventTimestampComparison == 0) {
            return o2.getEventTypeCode() - o1.getEventTypeCode();
        }
        return eventTimestampComparison;
    };


    @Test
    public void comparator() {
        AgentEvent a1 = new AgentEvent("a1", 0, 1, AgentEventType.AGENT_CONNECTED);
        AgentEvent a2 = new AgentEvent("a2", 0, 2, AgentEventType.AGENT_PING);
        AgentEvent a3 = new AgentEvent("a3", 0, 3, AgentEventType.AGENT_DEADLOCK_DETECTED);
        AgentEvent a4 = new AgentEvent("a4", 0, 3, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN);
        AgentEvent a5 = new AgentEvent("a4", 0, 3, AgentEventType.AGENT_CLOSED_BY_SERVER);

        List<AgentEvent> list1 = new ArrayList<>(Arrays.asList(a1, a2, a3, a4));
        Collections.shuffle(list1);
        list1.sort(AgentEvent.EVENT_TIMESTAMP_ASC_COMPARATOR);

        List<AgentEvent> list2 = new ArrayList<>(Arrays.asList(a1, a2, a3, a4));
        Collections.shuffle(list2);
        list2.sort(LEGACY_COMPARATOR);

        Assertions.assertEquals(list1, list2);

    }

}