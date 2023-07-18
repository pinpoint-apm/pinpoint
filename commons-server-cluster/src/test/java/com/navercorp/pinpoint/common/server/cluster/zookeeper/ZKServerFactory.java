/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.server.cluster.zookeeper;

import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;

import java.util.Map;

public class ZKServerFactory {
    private ZKServerFactory() {
    }

    public static TestingServer create(int zookeeperPort) throws Exception {
        Map<String, Object> customProperties = Map.of(
                "metricsProvider.className", "org.apache.zookeeper.metrics.impl.NullMetricsProvider"
        );
        InstanceSpec spec = new InstanceSpec(null, zookeeperPort,
                -1, -1, true, -1,
                -1, -1,
                customProperties);
        return new TestingServer(spec, true);
    }
}
