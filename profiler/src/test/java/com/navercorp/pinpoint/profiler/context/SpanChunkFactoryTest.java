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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

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
        AgentInformation agentInformation = new AgentInformation("agentId", "applicationName", 0,0, "machineName", "127.0.0.1", ServiceType.STAND_ALONE,
                JvmUtils.getSystemProperty(SystemPropertyKey.JAVA_VERSION), Version.VERSION);
        SpanChunkFactory spanChunkFactory = new SpanChunkFactory(agentInformation);

        try {
            spanChunkFactory.create(new ArrayList<SpanEvent>());
            Assert.fail();
        } catch (Exception ignored) {
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
