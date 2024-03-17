/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentStatMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.CustomMetricMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.UriStatMapper;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentCustomMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.AgentStatMetricSnapshotBatch;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.monitor.metric.uri.AgentUriStatData;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class GrpcStatMessageConverter implements MessageConverter<MetricType, GeneratedMessageV3> {

    private final AgentStatMapper agentStatMapper;

    private final CustomMetricMapper customMetricMapper;

    private final UriStatMapper uriStatMapper;

    public GrpcStatMessageConverter(
            AgentStatMapper agentStatMapper,
            CustomMetricMapper customMetricMapper,
            UriStatMapper uriStatMapper
    ) {
        this.agentStatMapper = Objects.requireNonNull(agentStatMapper, "agentStatMapper");
        this.customMetricMapper = Objects.requireNonNull(customMetricMapper, "customMetricMapper");
        this.uriStatMapper = Objects.requireNonNull(uriStatMapper, "uriStatMapper");
    }

    @Override
    public GeneratedMessageV3 toMessage(MetricType message) {
        if (message instanceof AgentStatMetricSnapshotBatch) {
            final AgentStatMetricSnapshotBatch agentStatMetricSnapshotBatch = (AgentStatMetricSnapshotBatch) message;
            return agentStatMapper.map(agentStatMetricSnapshotBatch);
        } else if (message instanceof AgentStatMetricSnapshot) {
            final AgentStatMetricSnapshot agentStatMetricSnapshot = (AgentStatMetricSnapshot) message;
            return agentStatMapper.map(agentStatMetricSnapshot);
        } else if (message instanceof AgentCustomMetricSnapshotBatch) {
            final AgentCustomMetricSnapshotBatch agentCustomMetricSnapshotBatch = (AgentCustomMetricSnapshotBatch) message;
            return customMetricMapper.map(agentCustomMetricSnapshotBatch);
        } else if (message instanceof AgentUriStatData) {
            final AgentUriStatData agentUriStatData = (AgentUriStatData) message;
            return uriStatMapper.map(agentUriStatData);
        }
        return null;
    }
}