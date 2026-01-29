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

package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.grpc.trace.PAcceptEvent;
import com.navercorp.pinpoint.grpc.trace.PParentInfo;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GrpcSpanBinderTest {

    GrpcSpanBinder grpcSpanBinder = new GrpcSpanBinder();

    BindAttribute bindAttribute = new BindAttribute("agentId-1", "agentName-1", "appName-1", "serviceName",
            1234, System.currentTimeMillis());

    @Test
    void bindSpanBo_parentApplicationName_invalid() {

        PAcceptEvent event = PAcceptEvent.newBuilder()
                .setParentInfo(PParentInfo.newBuilder()
                        .setParentApplicationName("##invalidId")
                        .build())
                .build();
        PSpan span = newSpan(event);

        Assertions.assertThrows(IllegalArgumentException.class, () -> grpcSpanBinder.newSpanBo(span, bindAttribute));
    }

    @Test
    void bindSpanBo_parentApplicationName_valid() {

        PAcceptEvent event = PAcceptEvent.newBuilder()
                .setParentInfo(PParentInfo.newBuilder()
                        .setParentApplicationName("validId")
                        .setParentApplicationType(1000)
                        .build())
                .build();
        PSpan span = newSpan(event);

        SpanBo spanBo = grpcSpanBinder.newSpanBo(span, bindAttribute);
        Assertions.assertEquals("validId", spanBo.getParentApplicationName());
        Assertions.assertEquals(1000, spanBo.getParentApplicationServiceType());
    }

    private PSpan newSpan(PAcceptEvent acceptEvent) {
        PTransactionId transactionId = PTransactionId.newBuilder()
                .setAgentId("agentId")
                .setAgentStartTime(1234)
                .setSequence(1)
                .build();

        PSpan.Builder spanBuilder = PSpan.newBuilder();

        spanBuilder.setTransactionId(transactionId);
        if (acceptEvent != null) {
            spanBuilder.setAcceptEvent(acceptEvent);
        }

        return spanBuilder.build();
    }

    @Test
    void bindSpanBo_root_node() {

        PSpan span = newSpan(null);

        SpanBo spanBo = grpcSpanBinder.newSpanBo(span, bindAttribute);
        Assertions.assertNull(spanBo.getParentApplicationName());

    }
}