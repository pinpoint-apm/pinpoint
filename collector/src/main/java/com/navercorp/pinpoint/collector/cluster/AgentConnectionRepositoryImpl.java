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
package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.collector.service.AgentConnection;
import com.navercorp.pinpoint.realtime.collector.service.AgentConnectionRepository;
import com.navercorp.pinpoint.thrift.sender.message.CommandGrpcToThriftMessageConverter;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class AgentConnectionRepositoryImpl implements AgentConnectionRepository {

    private final StreamRouteHandler streamRouteHandler;

    private final CommandGrpcToThriftMessageConverter messageConverter = new CommandGrpcToThriftMessageConverter();

    public AgentConnectionRepositoryImpl(StreamRouteHandler streamRouteHandler) {
        this.streamRouteHandler = Objects.requireNonNull(streamRouteHandler, "streamRouteHandler");
    }

    @Override
    public AgentConnection getConnection(ClusterKey key) {
        ClusterPoint<?> clusterPoint = this.streamRouteHandler.findClusterPoint(key);
        if (clusterPoint == null) {
            return null;
        }

        return new AgentConnectionImpl(clusterPoint, this.messageConverter);
    }

}
