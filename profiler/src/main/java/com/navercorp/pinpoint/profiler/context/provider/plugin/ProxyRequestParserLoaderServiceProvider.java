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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParserProvider;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.DefaultProxyRequestParserLoaderService;
import com.navercorp.pinpoint.profiler.context.recorder.proxy.ProxyRequestParserLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author jaehong.kim
 */
public class ProxyRequestParserLoaderServiceProvider implements Provider<ProxyRequestParserLoaderService> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public ProxyRequestParserLoaderServiceProvider() {
    }

    @Override
    public ProxyRequestParserLoaderService get() {
        final List<ProxyRequestParserProvider> providerList = new ArrayList<ProxyRequestParserProvider>();
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final ServiceLoader<ProxyRequestParserProvider> parserProviders = ServiceLoader.load(ProxyRequestParserProvider.class, classLoader);
        for (ProxyRequestParserProvider provider : parserProviders) {
            providerList.add(provider);
        }

        return new DefaultProxyRequestParserLoaderService(providerList);
    }
}