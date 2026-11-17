/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActiveMQClientHeaderTest {

    @Test
    public void roundTrip() throws Exception {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();

        ActiveMQClientHeader.setTraceId(message, "agent^1^2");
        ActiveMQClientHeader.setSpanId(message, 100L);
        ActiveMQClientHeader.setParentSpanId(message, -200L);
        ActiveMQClientHeader.setFlags(message, (short) 3);
        ActiveMQClientHeader.setSampled(message, false);
        ActiveMQClientHeader.setParentApplicationName(message, "app");
        ActiveMQClientHeader.setParentApplicationType(message, (short) 1010);
        ActiveMQClientHeader.setParentServiceName(message, "service");

        Assertions.assertEquals("agent^1^2", ActiveMQClientHeader.getTraceId(message, null));
        Assertions.assertEquals(100L, ActiveMQClientHeader.getSpanId(message, -1L));
        Assertions.assertEquals(-200L, ActiveMQClientHeader.getParentSpanId(message, -1L));
        Assertions.assertEquals((short) 3, ActiveMQClientHeader.getFlags(message, (short) 0));
        Assertions.assertEquals(false, ActiveMQClientHeader.getSampled(message, true));
        Assertions.assertEquals("app", ActiveMQClientHeader.getParentApplicationName(message, null));
        Assertions.assertEquals((short) 1010, ActiveMQClientHeader.getParentApplicationType(message, (short) -1));
        Assertions.assertEquals("service", ActiveMQClientHeader.getParentServiceName(message, null));
    }

    @Test
    public void writesEvenWhenPropertiesAreReadOnly() throws Exception {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setReadOnlyProperties(true);

        ActiveMQClientHeader.setTraceId(message, "agent^1^2");

        Assertions.assertEquals("agent^1^2", ActiveMQClientHeader.getTraceId(message, null));
        Assertions.assertTrue(message.isReadOnlyProperties());
    }

    @Test
    public void readsTheSameValueThroughTheJmsApi() throws Exception {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();

        ActiveMQClientHeader.setTraceId(message, "agent^1^2");
        ActiveMQClientHeader.setSpanId(message, 100L);

        Assertions.assertEquals("agent^1^2", message.getStringProperty(ActiveMQClientHeader.ACTIVEMQ_TRACE_ID.getId()));
        Assertions.assertEquals(100L, message.getLongProperty(ActiveMQClientHeader.ACTIVEMQ_SPAN_ID.getId()));
    }

    @Test
    public void missingPropertyReturnsDefault() {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();

        Assertions.assertNull(ActiveMQClientHeader.getTraceId(message, null));
        Assertions.assertEquals(-1L, ActiveMQClientHeader.getSpanId(message, -1L));
        Assertions.assertEquals((short) 0, ActiveMQClientHeader.getFlags(message, (short) 0));
        Assertions.assertEquals(true, ActiveMQClientHeader.getSampled(message, true));
    }

    @Test
    public void readsLeniently() throws Exception {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();
        // a String property arrives over the wire as a UTF8Buffer
        message.setProperty(ActiveMQClientHeader.ACTIVEMQ_TRACE_ID.getId(), new UTF8Buffer("agent^1^2"));
        // numbers and booleans set as text, or as another numeric type, by another client
        message.setProperty(ActiveMQClientHeader.ACTIVEMQ_SPAN_ID.getId(), "100");
        message.setProperty(ActiveMQClientHeader.ACTIVEMQ_PARENT_SPAN_ID.getId(), 200);
        message.setProperty(ActiveMQClientHeader.ACTIVEMQ_FLAGS.getId(), "3");
        message.setProperty(ActiveMQClientHeader.ACTIVEMQ_SAMPLED.getId(), "false");

        Assertions.assertEquals("agent^1^2", ActiveMQClientHeader.getTraceId(message, null));
        Assertions.assertEquals(100L, ActiveMQClientHeader.getSpanId(message, -1L));
        Assertions.assertEquals(200L, ActiveMQClientHeader.getParentSpanId(message, -1L));
        Assertions.assertEquals((short) 3, ActiveMQClientHeader.getFlags(message, (short) 0));
        Assertions.assertEquals(false, ActiveMQClientHeader.getSampled(message, true));
    }

    @Test
    public void unreadableNumberReturnsDefault() throws Exception {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setProperty(ActiveMQClientHeader.ACTIVEMQ_SPAN_ID.getId(), "not-a-number");

        Assertions.assertEquals(-1L, ActiveMQClientHeader.getSpanId(message, -1L));
    }
}
