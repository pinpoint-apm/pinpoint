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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class GrpcAgentConnectionRepositoryTest {

    @Mock ServerCallStreamObserver<PCmdRequest> observer1;
    @Mock ServerCallStreamObserver<PCmdRequest> observer2;
    @Mock ServerCallStreamObserver<PCmdRequest> observer3;

    @Test
    public void test() {
        GrpcAgentConnectionRepository repo = new GrpcAgentConnectionRepository();
        assertThat(repo.getConnections()).isEmpty();
        repo.add(mockConnection("a:b:1", observer1));
        repo.add(mockConnection("a:b:1", observer2));
        repo.add(mockConnection("a:b:2", observer3));
        assertThat(repo.getConnections()).hasSize(3);
        assertThat(repo.getConnection(ClusterKey.parse("a:b:1"))).isNotNull();
        assertThat(repo.getConnection(ClusterKey.parse("a:b:2"))).isNotNull();
        assertThat(repo.getConnection(ClusterKey.parse("a:b:3"))).isNull();
    }

    private GrpcAgentConnection mockConnection(String clusterKey, ServerCallStreamObserver<PCmdRequest> observer) {
        return new GrpcAgentConnection(
                InetSocketAddress.createUnresolved("127.0.0.1", 9999),
                ClusterKey.parse(clusterKey),
                observer,
                List.of()
        );
    }

}
