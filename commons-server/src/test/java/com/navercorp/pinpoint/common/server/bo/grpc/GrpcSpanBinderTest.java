package com.navercorp.pinpoint.common.server.bo.grpc;

import com.navercorp.pinpoint.grpc.trace.PAcceptEvent;
import com.navercorp.pinpoint.grpc.trace.PParentInfo;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PTransactionId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GrpcSpanBinderTest {

    @Test
    void bindSpanBo_InvalidParentApplicationName() {

        BindAttribute bindAttribute = new BindAttribute("agentId-1", "appName-1", 1234, System.currentTimeMillis());
        GrpcSpanBinder grpcSpanBinder = new GrpcSpanBinder();

        PAcceptEvent event = PAcceptEvent.newBuilder()
                .setParentInfo(PParentInfo.newBuilder()
                        .setParentApplicationName("##invalidId")
                        .build())
                .build();
        PTransactionId transactionId = PTransactionId.newBuilder()
                .setAgentId("agentId")
                .setAgentStartTime(1234)
                .setSequence(1)
                .build();

        PSpan span = PSpan.newBuilder()
                .setTransactionId(transactionId)
                .setAcceptEvent(event)
                .build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> grpcSpanBinder.newSpanBo(span, bindAttribute));
    }
}