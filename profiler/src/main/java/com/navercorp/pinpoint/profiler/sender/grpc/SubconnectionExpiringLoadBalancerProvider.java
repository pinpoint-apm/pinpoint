/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import io.grpc.LoadBalancer;
import io.grpc.LoadBalancerProvider;

import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
public class SubconnectionExpiringLoadBalancerProvider extends LoadBalancerProvider {
    private final long renewTransportPeriodMillis;

    public SubconnectionExpiringLoadBalancerProvider(GrpcTransportConfig config) {
        this.renewTransportPeriodMillis = config.getRenewTransportPeriodMillis();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public String getPolicyName() {
        return "subconnection_expiring_pick_first";
    }

    @Override
    public LoadBalancer newLoadBalancer(LoadBalancer.Helper helper) {
        return new SubconnectionExpiringLoadBalancer(helper, renewTransportPeriodMillis, TimeUnit.MILLISECONDS);
    }

}

