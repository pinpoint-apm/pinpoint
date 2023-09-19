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
package com.navercorp.pinpoint.web.realtime;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.web.service.ActiveThreadDumpService;

import java.util.List;

/**
 * @author youngjin.kim2
 */
public class EmptyActiveThreadDumpService implements ActiveThreadDumpService {

    @Override
    public PCmdActiveThreadLightDumpRes getLightDump(ClusterKey clusterKey, List<String> threadNames, List<Long> localTraceIds, int limit) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public PCmdActiveThreadDumpRes getDetailedDump(ClusterKey clusterKey, List<String> threadNames, List<Long> localTraceIds, int limit) {
        throw new RuntimeException("Not implemented");
    }

}
