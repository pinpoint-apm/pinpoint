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
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnection;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnectionRepository;
import com.navercorp.pinpoint.realtime.collector.service.ActiveThreadCountService;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class GrpcActiveThreadCountServiceTest {

    private static final long SINK_ID = 0;

    @Mock SinkRepository<FluxSink<PCmdActiveThreadCountRes>> sinkRepository;
    @Mock GrpcAgentConnection connection;
    @Mock GrpcAgentConnectionRepository connectionRepository;
    @Test
    public void test() {
        AtomicInteger seqCounter = new AtomicInteger(0);
        AtomicReference<FluxSink<Object>> sinkRef = new AtomicReference<>();

        doAnswer(inv -> {
            sinkRef.set(inv.getArgument(0));
            return SINK_ID;
        }).when(sinkRepository).put(any());

        doAnswer(inv -> {
            PCmdRequest req = inv.getArgument(0, PCmdRequest.class);
            assertThat(req.getRequestId()).isEqualTo(SINK_ID);
            assertThat(req.getCommandCase()).isEqualTo(PCmdRequest.CommandCase.COMMANDACTIVETHREADCOUNT);

            FluxSink<Object> sink = sinkRef.get();
            sink.next(mockResponse(seqCounter.incrementAndGet()));
            sink.next(mockResponse(seqCounter.incrementAndGet()));
            sink.next(mockResponse(seqCounter.incrementAndGet()));
            sink.complete();
            return null;
        }).when(connection).request(any());

        doReturn(List.of(TCommandType.ACTIVE_THREAD_COUNT)).when(connection)
                .getSupportCommandList();

        doReturn(connection).when(connectionRepository)
                .getConnection(ClusterKey.parse("application-name:agent-id:1234"));

        ActiveThreadCountService service = new GrpcActiveThreadCountService(
                connectionRepository,
                sinkRepository,
                Duration.ofHours(1000)
        );

        ATCDemand demand = new ATCDemand();
        demand.setId(0);
        demand.setApplicationName("application-name");
        demand.setAgentId("agent-id");
        demand.setStartTimestamp(1234);

        List<ATCSupply> supplies = service.requestAsync(demand).collectList().block(Duration.ofSeconds(1));
        assertThat(supplies).hasSize(3);
    }

    private static PCmdActiveThreadCountRes mockResponse(int seq) {
        return PCmdActiveThreadCountRes.newBuilder()
                .setHistogramSchemaType(0)
                .setTimeStamp(System.currentTimeMillis())
                .addAllActiveThreadCount(List.of(0, 1, 2, 3))
                .setCommonStreamResponse(PCmdStreamResponse.newBuilder()
                        .setResponseId((int) SINK_ID)
                        .setSequenceId(seq)
                        .setMessage(StringValue.of("Testing")))
                .build();
    }

}
