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
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnection;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnectionRepository;
import com.navercorp.pinpoint.realtime.collector.service.EchoService;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import com.navercorp.pinpoint.realtime.dto.Echo;
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
public class GrpcEchoServiceTest {

    private static final long SINK_ID = 0;

    @Mock SinkRepository<MonoSink<PCmdEchoResponse>> sinkRepository;
    @Mock GrpcAgentConnection connection;
    @Mock GrpcAgentConnectionRepository connectionRepository;
    @Test
    public void test() {
        AtomicReference<MonoSink<PCmdEchoResponse>> sinkRef = new AtomicReference<>();

        doAnswer(inv -> {
            sinkRef.set(inv.getArgument(0));
            return SINK_ID;
        }).when(sinkRepository).put(any());

        doAnswer(inv -> {
            PCmdRequest req = inv.getArgument(0, PCmdRequest.class);
            assertThat(req.getRequestId()).isEqualTo(SINK_ID);
            assertThat(req.getCommandCase()).isEqualTo(PCmdRequest.CommandCase.COMMANDECHO);

            MonoSink<PCmdEchoResponse> sink = sinkRef.get();
            sink.success(mockResponse());
            return null;
        }).when(connection).request(any());

        doReturn(List.of(TCommandType.ECHO)).when(connection)
                .getSupportCommandList();

        ClusterKey clusterKey = ClusterKey.parse("application-name:agent-id:1234");
        doReturn(connection).when(connectionRepository)
                .getConnection(clusterKey);

        EchoService service = new GrpcEchoService(connectionRepository, sinkRepository);

        Echo echo = new Echo(0, clusterKey, "PING");

        Echo res = service.echo(echo).block();
        assertThat(res).isNotNull();
        assertThat(res.getMessage()).isEqualTo("PING");
    }

    private static PCmdEchoResponse mockResponse() {
        return PCmdEchoResponse.newBuilder()
                .setMessage("PING")
                .setCommonResponse(PCmdResponse.newBuilder()
                        .setResponseId((int) SINK_ID)
                        .setMessage(StringValue.of("Testing")))
                .build();
    }

}
