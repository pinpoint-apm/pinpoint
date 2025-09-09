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

package com.navercorp.pinpoint.common.server.applicationmap;

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

public record Vertex(String applicationName, ServiceType serviceType) {

    public Vertex(String applicationName, ServiceType serviceType) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }


    public static Vertex of(String applicationName, ServiceType serviceType) {
        return new Vertex(applicationName, serviceType);
    }

    @Override
    public String toString() {
        return applicationName + "/" + serviceType.getName() + ':' + serviceType.getCode();
    }
}
