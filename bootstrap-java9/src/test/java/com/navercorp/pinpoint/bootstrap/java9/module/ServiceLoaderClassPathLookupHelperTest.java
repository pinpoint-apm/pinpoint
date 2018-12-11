/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.java9.module;

import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author jaehong.kim
 */
public class ServiceLoaderClassPathLookupHelperTest {

    @Test
    public void lookup() throws Exception {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ServiceLoaderClassPathLookupHelper lookup = new ServiceLoaderClassPathLookupHelper(classLoader);

        // FileSystem
        // com.navercorp.pinpoint.common.trace.proxy.ProxyRequestMetadataProvider=[com.navercorp.pinpoint.agent.plugin.proxy.apache.ApacheRequestMetadataProvider]
        Set<String> proxyRequestMetadataProviderSet = lookup.lookup("com.navercorp.pinpoint.common.trace.proxy.ProxyRequestMetadataProvider");
        assertNotNull(proxyRequestMetadataProviderSet);
        assertEquals(1, proxyRequestMetadataProviderSet.size());
        assertTrue(proxyRequestMetadataProviderSet.contains("com.navercorp.pinpoint.agent.plugin.proxy.apache.ApacheRequestMetadataProvider"));

        // com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParserProvider=[com.navercorp.pinpoint.agent.plugin.proxy.apache.ApacheRequestParserProvider]
        Set<String> proxyRequestParserProviderSet = lookup.lookup("com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParserProvider");
        assertNotNull(proxyRequestParserProviderSet);
        assertEquals(1, proxyRequestParserProviderSet.size());
        assertTrue(proxyRequestParserProviderSet.contains("com.navercorp.pinpoint.agent.plugin.proxy.apache.ApacheRequestParserProvider"));

        // Jar
        // org.apache.commons.logging.LogFactory=[org.apache.commons.logging.impl.SLF4JLogFactory]

        Set<String> logFactorySet = lookup.lookup("org.apache.commons.logging.LogFactory");
        assertNotNull(logFactorySet);
        assertEquals(1, logFactorySet.size());
        assertTrue(logFactorySet.contains("org.apache.commons.logging.impl.SLF4JLogFactory"));
    }
}