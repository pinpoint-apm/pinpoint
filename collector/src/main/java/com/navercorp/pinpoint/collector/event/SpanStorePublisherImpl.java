/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.event;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.event.ContextData;
import com.navercorp.pinpoint.common.server.event.ContextSupplier;
import com.navercorp.pinpoint.common.server.event.SpanChunkInsertEvent;
import com.navercorp.pinpoint.common.server.event.SpanInsertEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SpanStorePublisherImpl implements SpanStorePublisher {
    private final ApplicationEventPublisher publisher;
    private final ContextSupplier supplier;

    public SpanStorePublisherImpl(ApplicationEventPublisher publisher, ContextSupplier supplier) {
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.supplier = Objects.requireNonNull(supplier, "supplier");
    }

    @Override
    public SpanInsertEvent captureContext(SpanBo spanBo) {
        ContextData contextData = supplier.applyAsContext(spanBo);
        return new SpanInsertEvent(contextData, false);
    }

    @Override
    public SpanChunkInsertEvent captureContext(SpanChunkBo spanChunkBo) {
        ContextData contextData = supplier.applyAsContext(spanChunkBo);
        return new SpanChunkInsertEvent(contextData, false);
    }

    @Override
    public void publishEvent(SpanInsertEvent event, boolean success) {
        publisher.publishEvent(new SpanInsertEvent(event.getContextData(), success));
    }

    @Override
    public void publishEvent(SpanChunkInsertEvent event, boolean success) {
        publisher.publishEvent(new SpanChunkInsertEvent(event.getContextData(), success));
    }
}
