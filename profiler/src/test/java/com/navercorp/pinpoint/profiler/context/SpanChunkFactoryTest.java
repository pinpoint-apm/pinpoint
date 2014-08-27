package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.profiler.AgentInformation;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanChunkFactoryTest {
    @Test
    public void create() {
        AgentInformation agentInformation = new AgentInformation("agentId", "applicationName", 0,0, "machineName", ServiceType.TOMCAT.getCode(), Version.VERSION);
        SpanChunkFactory spanChunkFactory = new SpanChunkFactory(agentInformation);

        try {
            spanChunkFactory.create(new ArrayList<SpanEvent>());
            Assert.fail();
        } catch (Exception e) {
        }
        // one spanEvent
        List<SpanEvent> spanEvents = new ArrayList<SpanEvent>();
        spanEvents.add(new SpanEvent(new Span()));
        spanChunkFactory.create(spanEvents);

        // two spanEvent
        spanEvents.add(new SpanEvent(new Span()));
        spanChunkFactory.create(spanEvents);

        // three
        spanEvents.add(new SpanEvent(new Span()));
        spanChunkFactory.create(spanEvents);

    }
}
