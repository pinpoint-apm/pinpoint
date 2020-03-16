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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class RequestUrlMappingExtractorProviderRegistryTest {

    @Test
    public void providerRegistryTest() {
        RequestUrlMappingExtractorProvider provider1
                = new RequestUrlMappingExtractorProvider(RequestUrlMappingExtractorType.SERVLET_REQUEST_ATTRIBUTE, "hello");

        RequestUrlMappingExtractorProvider provider2
                = new RequestUrlMappingExtractorProvider(RequestUrlMappingExtractorType.SERVLET_REQUEST_ATTRIBUTE, "hi");


        RequestUrlMappingExtractorProviderRegistry registry
                = new RequestUrlMappingExtractorProviderRegistry(Arrays.asList(provider1, provider2));

        List<RequestUrlMappingExtractorProvider> result = registry.get(RequestUrlMappingExtractorType.SERVLET_REQUEST_ATTRIBUTE);
        Assert.assertEquals(2, result.size());

        RequestUrlMappingExtractorType mockType = Mockito.mock(RequestUrlMappingExtractorType.class);

        result = registry.get(mockType);
        Assert.assertEquals(0, result.size());
    }

}
