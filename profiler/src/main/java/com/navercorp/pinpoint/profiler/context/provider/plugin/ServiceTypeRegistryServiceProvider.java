/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.loader.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.loader.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServiceTypeRegistryServiceProvider implements Provider<ServiceTypeRegistryService> {

    private final TraceMetadataLoaderService traceMetadataLoaderService;

    @Inject
    public ServiceTypeRegistryServiceProvider(TraceMetadataLoaderService traceMetadataLoaderService) {
        this.traceMetadataLoaderService = Assert.requireNonNull(traceMetadataLoaderService, "traceMetadataLoaderService");
    }

    @Override
    public ServiceTypeRegistryService get() {
        ServiceTypeRegistryService serviceTypeRegistryService = new DefaultServiceTypeRegistryService(traceMetadataLoaderService);
        return serviceTypeRegistryService ;
    }
}
