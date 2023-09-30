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
package com.navercorp.pinpoint.realtime.collector.receiver.grpc;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import io.grpc.stub.ServerCallStreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class GrpcAgentConnectionTest {
    @Mock ServerCallStreamObserver<PCmdRequest> requestObserver;

    @Test
    public void testRequest() {
        PCmdRequest command = PCmdRequest.getDefaultInstance();

        doNothing().when(requestObserver).onNext(eq(command));

        GrpcAgentConnection conn = new GrpcAgentConnection(
                InetSocketAddress.createUnresolved("127.0.0.1", 9999),
                ClusterKey.parse("application-name:agent-id:0"),
                requestObserver,
                List.of()
        );
        conn.request(command);

        verify(requestObserver).onNext(eq(command));
    }

}
