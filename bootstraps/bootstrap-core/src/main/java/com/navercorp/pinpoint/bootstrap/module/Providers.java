/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.module;

import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Providers {
    private final String services;
    private final List<String> providers;

    public Providers(String services, List<String> providers) {
        this.services = Objects.requireNonNull(services, "services");
        this.providers = Objects.requireNonNull(providers, "providers");
    }

    public String getService() {
        return services;
    }

    public String getServicePackage() {
        int lastDotIndex = services.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return services;
        }
        return services.substring(0, lastDotIndex);
    }

    public List<String> getProviders() {
        return providers;
    }

    @Override
    public String toString() {
        return "Providers{" +
                "services='" + services + '\'' +
                ", providers=" + providers +
                '}';
    }
}
