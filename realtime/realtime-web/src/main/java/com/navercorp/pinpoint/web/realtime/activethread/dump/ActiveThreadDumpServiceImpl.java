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
package com.navercorp.pinpoint.web.realtime.activethread.dump;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoClient;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.realtime.dto.mapper.grpc.GrpcDtoMapper;
import com.navercorp.pinpoint.web.service.ActiveThreadDumpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class ActiveThreadDumpServiceImpl implements ActiveThreadDumpService {

    private static final Logger logger = LogManager.getLogger(ActiveThreadDumpServiceImpl.class);

    private final PubSubMonoClient<ATDDemand, ATDSupply> endpoint;

    ActiveThreadDumpServiceImpl(PubSubMonoClient<ATDDemand, ATDSupply> endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
    }

    @Override
    public PCmdActiveThreadLightDumpRes getLightDump(ClusterKey clusterKey, List<String> threadNames,
                                                     List<Long> localTraceIds, int limit) {
        final ATDSupply supply = get(clusterKey, threadNames, localTraceIds, limit, true);
        return GrpcDtoMapper.buildLightDumpResult(supply);
    }

    @Override
    public PCmdActiveThreadDumpRes getDetailedDump(ClusterKey clusterKey, List<String> threadNames,
                                                   List<Long> localTraceIds, int limit) {
        final ATDSupply supply = get(clusterKey, threadNames, localTraceIds, limit, false);
        return GrpcDtoMapper.buildDetailedDumpResult(supply);
    }

    private ATDSupply get(
            ClusterKey clusterKey,
            List<String> threadNames,
            List<Long> localTraceIds,
            int limit,
            boolean isLight
    ) {
        final ATDDemand demand = new ATDDemand();
        demand.setClusterKey(clusterKey);
        demand.setLight(isLight);
        demand.setLimit(limit);
        demand.setThreadNameList(threadNames);
        demand.setLocalTraceIdList(localTraceIds);

        final Mono<ATDSupply> supplyFuture = this.endpoint.request(demand);
        try {
            return supplyFuture.block();
        } catch (Exception e) {
            logger.error("Failed to getSession activeThreadDump", e);
            throw new RuntimeException("Failed to getSession activeThreadDump", e);
        }
    }

}
