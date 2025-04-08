package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.grpc.trace.PAcceptEvent;
import com.navercorp.pinpoint.grpc.trace.PParentInfo;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GrpcSpanBinderTest {

    GrpcSpanBinder grpcSpanBinder = new GrpcSpanBinder();

    BindAttribute bindAttribute = new BindAttribute("agentId-1", "agentName-1", "appName-1", () -> ApplicationUid.of(100), 1234, System.currentTimeMillis());

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