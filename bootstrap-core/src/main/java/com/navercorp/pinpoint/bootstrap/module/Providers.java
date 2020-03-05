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

import com.navercorp.pinpoint.common.util.Assert;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Providers {
    private final String services;
    private final List<String> providers;

    public Providers(String services, List<String> providers) {
        this.services = Assert.requireNonNull(services, "services");
        this.providers = Assert.requireNonNull(providers, "providers");
    }

    public String getService() {
        return services;
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
