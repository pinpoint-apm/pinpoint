/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author jaehong.kim
 */
public class SpanEventFactoryTest {

    @Test
    public void newInstance() {
        SpanEventFactory factory = new SpanEventFactory();
        final SpanEvent spanEvent = factory.newInstance();
        assertNotNull(spanEvent);
    }

    @Test
    public void markDepth() {
        SpanEventFactory factory = new SpanEventFactory();
        final SpanEvent spanEvent = factory.newInstance();
        factory.markDepth(spanEvent, 1);
        assertEquals(1, spanEvent.getDepth());
    }

    @Test
    public void setSequence() {
        SpanEventFactory factory = new SpanEventFactory();
        final SpanEvent spanEvent = factory.newInstance();
        factory.setSequence(spanEvent, 999);
        assertEquals(999, spanEvent.getSequence());
    }

    @Test
    public void dummy() {
        SpanEventFactory factory = new SpanEventFactory();
        final SpanEvent dummy = factory.disableInstance();
        assertTrue(factory.isDisable(dummy));

        final SpanEvent spanEvent = factory.newInstance();
        assertFalse(factory.isDisable(spanEvent));
    }
}