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
package com.navercorp.pinpoint.realtime.collector.service;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;

/**
 * @author youngjin.kim2
 */
public interface AgentCommandService {

    /**
     * Find an active agent which has the cluster key and send the command to the agent.
     * If the agent is not found, return null.
     * @param clusterKey the cluster key of agent
     * @param command command
     * @param durationMillis the request is closed internally after the duration
     * @return Flux of response from agent
     */
    @Nullable
    Flux<GeneratedMessageV3> requestStream(ClusterKey clusterKey, GeneratedMessageV3 command, long durationMillis);

    /**
     * Find an active agent which has the cluster key and send the command to the agent.
     * If the agent is not found, return null.
     * @param clusterKey the cluster key of agent
     * @param command command
     * @return Mono of response from agent
     */
    @Nullable
    Mono<GeneratedMessageV3> request(ClusterKey clusterKey, GeneratedMessageV3 command);

}
