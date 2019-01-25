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

package com.navercorp.pinpoint.plugin.nodejs;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

import static com.navercorp.pinpoint.common.trace.ServiceTypeFactory.of;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author jaehong.kim
 */
public class NodeTypeProvider implements TraceMetadataProvider {
    @Override
    public void setup(TraceMetadataSetupContext context) {
        // Node.js
        final ServiceType NODE = of(1400, "NODE", RECORD_STATISTICS);
        final ServiceType NODE_METHOD = of(1401, "NODE_METHOD", "NODE");
        context.addServiceType(NODE);
        context.addServiceType(NODE_METHOD);

        // Fast, unopinionated, minimalist web framework for Node.js
        final ServiceType EXPRESS = of(6600, "EXPRESS");
        context.addServiceType(EXPRESS);

        // Next generation web framework for Node.js
        final ServiceType KOA = of(6610, "KOA");
        context.addServiceType(KOA);

        // A rich framework for building applications and services
        final ServiceType HAPI = of(6620, "HAPI");
        context.addServiceType(HAPI);

        // The future of Node.js REST development
        final ServiceType RESTIFY = of(6630, "RESTIFY");
        context.addServiceType(RESTIFY);

        // A robust, performance-focused and full-featured Redis client for Node.js.
        final ServiceType IOREDIS = of(8202, "IOREDIS", "REDIS");
        context.addServiceType(IOREDIS);
    }
}