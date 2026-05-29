/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.jspecify.annotations.NonNull;

import java.util.function.Supplier;

/**
 * Minimal ServerHeader used to reconstruct a buffered span at flush time.
 * Only the four fields actually read by GrpcSpanBinder carry meaning.
 */
public class ReconstructedServerHeader implements ServerHeader {

    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final long agentStartTime;

    public ReconstructedServerHeader(String agentId, String agentName, String applicationName, long agentStartTime) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.applicationName = applicationName;
        this.agentStartTime = agentStartTime;
    }

    @NonNull
    @Override
    public String getAgentId() { return agentId; }

    @NonNull
    @Override
    public String getAgentName() { return agentName; }

    @NonNull
    @Override
    public String getApplicationName() { return applicationName; }

    @Override
    public String getServiceName() { return null; }

    @Override
    public Supplier<ServiceUid> getServiceUid() { return () -> ServiceUid.DEFAULT; }

    @Override
    public long getAgentStartTime() { return agentStartTime; }

    @Override
    public int getServiceType() { return 0; }

    @Override
    public boolean isGrpcBuiltInRetry() { return false; }
}
