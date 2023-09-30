/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime.collector.service.grpc;

import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnection;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnectionRepository;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadDumpService;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.MonoSink;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class GrpcActiveThreadDumpServiceTest {

    private static final long SINK_ID = 0;

    @Mock SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> detailSinkRepository;
    @Mock SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> lightSinkRepository;
    @Mock GrpcAgentConnection connection;
    @Mock GrpcAgentConnectionRepository connectionRepository;
    @Test
    public void testDetailDump() {
        AtomicReference<MonoSink<PCmdActiveThreadDumpRes>> sinkRef = new AtomicReference<>();

        doAnswer(inv -> {
            sinkRef.set(inv.getArgument(0));
            return SINK_ID;
        }).when(detailSinkRepository).put(any());

        doAnswer(inv -> {
            PCmdRequest req = inv.getArgument(0, PCmdRequest.class);
            assertThat(req.getRequestId()).isEqualTo(SINK_ID);
            assertThat(req.getCommandCase()).isEqualTo(PCmdRequest.CommandCase.COMMANDACTIVETHREADDUMP);

            MonoSink<PCmdActiveThreadDumpRes> sink = sinkRef.get();
            sink.success(mockDetailResponse());
            return null;
        }).when(connection).request(any());

        doReturn(List.of(TCommandType.ACTIVE_THREAD_DUMP)).when(connection)
                .getSupportCommandList();

        doReturn(connection).when(connectionRepository)
                .getConnection(ClusterKey.parse("application-name:agent-id:1234"));

        ActiveThreadDumpService service = new GrpcActiveThreadDumpService(
                connectionRepository,
                detailSinkRepository,
                lightSinkRepository
        );

        ATDDemand demand = new ATDDemand();
        demand.setId(0);
        demand.setClusterKey(ClusterKey.parse("application-name:agent-id:1234"));
        demand.setLight(false);

        ATDSupply dump = service.getDump(demand).block();
        assertThat(dump).isNotNull();
    }

    private static PCmdActiveThreadDumpRes mockDetailResponse() {
        return PCmdActiveThreadDumpRes.newBuilder()
                .setCommonResponse(PCmdResponse.newBuilder()
                        .setResponseId((int) SINK_ID)
                        .setMessage(StringValue.of("Testing")))
                .build();
    }

    @Test
    public void testLightDump() {
        AtomicReference<MonoSink<PCmdActiveThreadLightDumpRes>> sinkRef = new AtomicReference<>();

        doAnswer(inv -> {
            sinkRef.set(inv.getArgument(0));
            return SINK_ID;
        }).when(lightSinkRepository).put(any());

        doAnswer(inv -> {
            PCmdRequest req = inv.getArgument(0, PCmdRequest.class);
            assertThat(req.getRequestId()).isEqualTo(SINK_ID);
            assertThat(req.getCommandCase()).isEqualTo(PCmdRequest.CommandCase.COMMANDACTIVETHREADLIGHTDUMP);

            MonoSink<PCmdActiveThreadLightDumpRes> sink = sinkRef.get();
            sink.success(mockLightResponse());
            return null;
        }).when(connection).request(any());

        doReturn(List.of(TCommandType.ACTIVE_THREAD_LIGHT_DUMP)).when(connection)
                .getSupportCommandList();

        doReturn(connection).when(connectionRepository)
                .getConnection(ClusterKey.parse("application-name:agent-id:1234"));

        ActiveThreadDumpService service = new GrpcActiveThreadDumpService(
                connectionRepository,
                detailSinkRepository,
                lightSinkRepository
        );

        ATDDemand demand = new ATDDemand();
        demand.setId(0);
        demand.setClusterKey(ClusterKey.parse("application-name:agent-id:1234"));
        demand.setLight(true);

        ATDSupply dump = service.getDump(demand).block();
        assertThat(dump).isNotNull();
    }

    private static PCmdActiveThreadLightDumpRes mockLightResponse() {
        return PCmdActiveThreadLightDumpRes.newBuilder()
                .setCommonResponse(PCmdResponse.newBuilder()
                        .setResponseId((int) SINK_ID)
                        .setMessage(StringValue.of("Testing")))
                .build();
    }

}
