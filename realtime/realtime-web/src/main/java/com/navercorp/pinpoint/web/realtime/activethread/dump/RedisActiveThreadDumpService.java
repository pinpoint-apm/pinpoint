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
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.redis.value.Incrementer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class RedisActiveThreadDumpService {

    private static final Logger logger = LogManager.getLogger(RedisActiveThreadDumpService.class);

    private final Incrementer incrementer;
    private final ActiveThreadDumpDao dao;

    public RedisActiveThreadDumpService(Incrementer incrementer, ActiveThreadDumpDao dao) {
        this.incrementer = Objects.requireNonNull(incrementer, "incrementer");
        this.dao = Objects.requireNonNull(dao, "dao");
    }

    public ATDSupply getLightDump(ClusterKey clusterKey, List<String> threadNames,
                                                     List<Long> localTraceIds, int limit) {
        return get(clusterKey, threadNames, localTraceIds, limit, true);
    }

    public ATDSupply getDetailedDump(ClusterKey clusterKey, List<String> threadNames,
                                                   List<Long> localTraceIds, int limit) {
        return get(clusterKey, threadNames, localTraceIds, limit, false);
    }

    private ATDSupply get(
            ClusterKey clusterKey,
            List<String> threadNames,
            List<Long> localTraceIds,
            int limit,
            boolean isLight
    ) {
        long id = this.incrementer.get();

        ATDDemand demand = new ATDDemand();
        demand.setId(id);
        demand.setClusterKey(clusterKey);
        demand.setLight(isLight);
        demand.setLimit(limit);
        demand.setThreadNameList(threadNames);
        demand.setLocalTraceIdList(localTraceIds);

        final Mono<ATDSupply> supplyFuture = this.dao.dump(demand);
        try {
            return supplyFuture.block();
        } catch (Exception e) {
            logger.error("Failed to getSession activeThreadDump", e);
            throw new RuntimeException("Failed to getSession activeThreadDump", e);
        }
    }

}
