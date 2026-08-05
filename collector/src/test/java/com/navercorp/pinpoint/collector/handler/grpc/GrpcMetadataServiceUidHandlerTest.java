package com.navercorp.pinpoint.collector.handler.grpc;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.collector.service.SqlMetaDataService;
import com.navercorp.pinpoint.collector.service.SqlUidMetaDataService;
import com.navercorp.pinpoint.collector.service.StringMetaDataService;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.FixedServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class GrpcMetadataServiceUidHandlerTest {
    private static final ServiceUid SERVICE = ServiceUid.of(100_001);

    @Test
    void sqlHandlerPropagatesServiceUid() {
        SqlMetaDataService service = mock(SqlMetaDataService.class);
        GrpcSqlMetaDataHandler handler = new GrpcSqlMetaDataHandler(
                new SqlMetaDataService[]{service});
        PResult result = handler.handleSqlMetaData(header(),
                PSqlMetaData.newBuilder().setSqlId(7).setSql("select 1").build());

        ArgumentCaptor<SqlMetaDataBo> captor = ArgumentCaptor.forClass(SqlMetaDataBo.class);
        verify(service).insert(captor.capture());
        assertThat(captor.getValue().getServiceUid()).isEqualTo(SERVICE);
        assertThat(result.getSuccess()).isTrue();
    }

    @Test
    void sqlUidHandlerPropagatesServiceUidAndRejectsWrongLength() {
        SqlUidMetaDataService service = mock(SqlUidMetaDataService.class);
        GrpcSqlUidMetaDataHandler handler = new GrpcSqlUidMetaDataHandler(
                new SqlUidMetaDataService[]{service});
        PResult success = handler.handleSqlUidMetaData(header(),
                PSqlUidMetaData.newBuilder()
                        .setSqlUid(ByteString.copyFrom(new byte[16]))
                        .setSql("select 1")
                        .build());

        ArgumentCaptor<SqlUidMetaDataBo> captor = ArgumentCaptor.forClass(SqlUidMetaDataBo.class);
        verify(service).insert(captor.capture());
        assertThat(captor.getValue().getServiceUid()).isEqualTo(SERVICE);
        assertThat(success.getSuccess()).isTrue();

        clearInvocations(service);
        PResult failure = handler.handleSqlUidMetaData(header(),
                PSqlUidMetaData.newBuilder()
                        .setSqlUid(ByteString.copyFrom(new byte[15]))
                        .setSql("select 1")
                        .build());
        assertThat(failure.getSuccess()).isFalse();
        verifyNoInteractions(service);
    }

    @Test
    void stringHandlerPropagatesServiceUid() {
        StringMetaDataService service = mock(StringMetaDataService.class);
        GrpcStringMetaDataHandler handler = new GrpcStringMetaDataHandler(
                service);
        PResult result = handler.handleStringMetaData(header(),
                PStringMetaData.newBuilder().setStringId(7).setStringValue("value").build());

        ArgumentCaptor<StringMetaDataBo> captor = ArgumentCaptor.forClass(StringMetaDataBo.class);
        verify(service).insert(captor.capture());
        assertThat(captor.getValue().getServiceUid()).isEqualTo(SERVICE);
        assertThat(result.getSuccess()).isTrue();
    }

    private static ServerHeader header() {
        ServerHeader header = mock(ServerHeader.class);
        when(header.getAgentId()).thenReturn("agent");
        when(header.getAgentStartTime()).thenReturn(10L);
        when(header.getApplicationName()).thenReturn("app");
        when(header.getServiceName()).thenReturn("service");
        when(header.getServiceUid()).thenReturn(new FixedServiceUid(SERVICE));
        return header;
    }
}
