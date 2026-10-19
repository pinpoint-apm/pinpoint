/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector.linkindex;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntry;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexWriter;

import java.util.List;
import java.util.Objects;

/**
 * Records both sides of OTel calls whose callee did not receive the caller's Pinpoint parent
 * application, so that the join job can draw {@code caller -> callee} on the server map.
 * <ul>
 *     <li>SERVER entry: a span with a parent, without parentApplication, and not a queue. The
 *     acceptorHost is not required: it is the endPoint, which is empty when the server reports no
 *     {@code server.address} (Ruby Rack only sets {@code http.host}). A CONSUMER span of an
 *     unsupported messaging system also passes; its parent is a PRODUCER, never written as a CLIENT
 *     entry, so it simply never matches.</li>
 *     <li>CLIENT entry: an RPC client span event (HTTP, gRPC, other RPC) with a next span id and a
 *     destination. Database, cache and messaging clients are skipped: no SERVER span answers them.
 *     The caller cannot know whether its callee received the parent application, so a CLIENT entry
 *     is written for every RPC call; those whose callee did have it simply never match.</li>
 * </ul>
 * Native Pinpoint spans never reach this service.
 */
public class LinkIndexTraceService implements TraceService {

    private final LinkIndexWriter writer;
    private final ServiceTypeRegistryService registry;

    public LinkIndexTraceService(LinkIndexWriter writer, ServiceTypeRegistryService registry) {
        this.writer = Objects.requireNonNull(writer, "writer");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public void insertSpan(SpanBo span) {
        final byte[] traceId = traceId(span.getTransactionId());
        if (traceId == null) {
            return;
        }
        final SpanOwner owner = span.getSpanOwner();
        final int serviceUid = owner.getServiceUid().getUid();
        final String applicationName = owner.getApplicationName();
        final int applicationServiceType = span.getApplicationServiceType();

        if (isServerWithoutParentApplication(span)) {
            writer.offer(LinkEntry.server(traceId, span.getParentSpanId(), serviceUid, applicationName,
                    applicationServiceType, span.getElapsed(), span.hasError()));
        }
        offerClients(traceId, serviceUid, applicationName, applicationServiceType, span.getSpanEventBoList());
    }

    @Override
    public void insertSpanChunk(SpanChunkBo spanChunk) {
        final byte[] traceId = traceId(spanChunk.getTransactionId());
        if (traceId == null) {
            return;
        }
        final SpanOwner owner = spanChunk.getSpanOwner();
        offerClients(traceId, owner.getServiceUid().getUid(), owner.getApplicationName(),
                spanChunk.getApplicationServiceType(), spanChunk.getSpanEventBoList());
    }

    boolean isServerWithoutParentApplication(SpanBo span) {
        if (span.getParentSpanId() == -1) {
            return false;
        }
        if (span.getParentApplication() != null) {
            return false;
        }
        final ServiceType serviceType = registry.findServiceType(span.getServiceType());
        return !serviceType.isQueue();
    }

    private void offerClients(byte[] traceId, int serviceUid, String applicationName, int applicationServiceType,
                              List<SpanEventBo> events) {
        if (events == null) {
            return;
        }
        for (SpanEventBo event : events) {
            if (isRpcClient(event)) {
                writer.offer(LinkEntry.client(traceId, event.getNextSpanId(), serviceUid, applicationName,
                        applicationServiceType, event.getDestinationId()));
            }
        }
    }

    boolean isRpcClient(SpanEventBo event) {
        if (event.getNextSpanId() == -1) {
            return false;
        }
        if (event.getDestinationId() == null) {
            return false;
        }
        return registry.findServiceType(event.getServiceType()).isRpcClient();
    }

    private static byte[] traceId(ServerTraceId transactionId) {
        if (transactionId instanceof OtelServerTraceId otel) {
            final byte[] id = otel.getId();
            if (id != null && id.length == LinkEntry.TRACE_ID_LENGTH) {
                return id;
            }
        }
        return null;
    }
}
