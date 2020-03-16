/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.monitor;

import com.navercorp.pinpoint.common.trace.RequestUrlMappingExtractorType;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class RequestUrlMappingExtractorProviderRegistry implements RequestUrlMappingExtractorProviderLocator {

    private final List<RequestUrlMappingExtractorProvider> providerList;

    public RequestUrlMappingExtractorProviderRegistry(List<RequestUrlMappingExtractorProvider> providerList) {
        this.providerList = Assert.requireNonNull(providerList, "providerList");
    }

    @Override
    public List<RequestUrlMappingExtractorProvider> getProviderList() {
        return providerList;
    }

    @Override
    public List<RequestUrlMappingExtractorProvider> get(RequestUrlMappingExtractorType type) {
        List<RequestUrlMappingExtractorProvider> result = new ArrayList<RequestUrlMappingExtractorProvider>();

        for (RequestUrlMappingExtractorProvider provider : providerList) {
            if (provider.getType() == type) {
                result.add(provider);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestUrlMappingExtractorProviderRegistry{");
        sb.append("providerList=").append(providerList);
        sb.append('}');
        return sb.toString();
    }
    
}
