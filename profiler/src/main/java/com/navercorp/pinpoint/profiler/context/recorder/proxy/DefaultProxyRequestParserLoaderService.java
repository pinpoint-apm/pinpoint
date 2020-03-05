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

package com.navercorp.pinpoint.profiler.context.recorder.proxy;

import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class DefaultProxyRequestParserLoaderService implements ProxyRequestParserLoaderService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final List<ProxyRequestParserProvider> providerList;
    private final List<ProxyRequestParser> proxyHttpHeaderParserList = new ArrayList<ProxyRequestParser>();

    public DefaultProxyRequestParserLoaderService(final List<ProxyRequestParserProvider> providerList) {
        this.providerList = Assert.requireNonNull(providerList, "providerList");
        load();
    }

    private void load() {
        logger.info("Loading ProxyRequestParserProvider");
        for (ProxyRequestParserProvider provider : providerList) {
            final ProxyRequestParserProviderSetupContext context = new ProxyRequestParserProviderSetupContext() {
                @Override
                public void addProxyRequestParser(ProxyRequestParser parser) {
                    if (parser != null) {
                        logger.info("Add ProxyRequestParser={}", parser);
                        proxyHttpHeaderParserList.add(parser);
                    }
                }
            };
            provider.setup(context);
        }
    }

    public List<ProxyRequestParser> getProxyRequestParserList() {
        return this.proxyHttpHeaderParserList;
    }
}