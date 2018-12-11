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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestMetadataProvider;
import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestMetadataSetupContext;
import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestType;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author jaehong.kim
 */
@Service
public class ProxyRequestTypeRegistryServiceImpl implements ProxyRequestTypeRegistryService {
    private static final ProxyRequestType UNKNOWN = new ProxyRequestType() {
        @Override
        public String getHttpHeaderName() {
            return "";
        }

        @Override
        public String getDisplayName() {
            return "PROXY(UNKNOWN)";
        }

        @Override
        public int getCode() {
            return 0;
        }
    };

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final IntHashMap<ProxyRequestType> codeLookupTable = new IntHashMap<ProxyRequestType>();

    public ProxyRequestTypeRegistryServiceImpl() {
    }

    @PostConstruct
    public void init() {
        final ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        final ServiceLoader<ProxyRequestMetadataProvider> serviceLoader = ServiceLoader.load(ProxyRequestMetadataProvider.class, classLoader);
        final List<ProxyRequestType> proxyRequestTypeList = new ArrayList<ProxyRequestType>();
        for (ProxyRequestMetadataProvider provider : serviceLoader) {
            final ProxyRequestMetadataSetupContext context = new ProxyRequestMetadataSetupContext() {
                @Override
                public void addProxyHttpHeaderType(ProxyRequestType type) {
                    proxyRequestTypeList.add(type);
                }
            };
            provider.setup(context);
        }
        logger.info("Loading ProxyRequestTypeProvider {}", proxyRequestTypeList);

        for (ProxyRequestType type : proxyRequestTypeList) {
            logger.info("Add ProxyRequestType {}", type);
            final ProxyRequestType exist = this.codeLookupTable.put(type.getCode(), type);
            if (exist != null) {
                logger.warn("Duplicated ProxyRequestType {}/{}", type, exist);
            }
        }
    }

    public ProxyRequestType findByCode(final int code) {
        final ProxyRequestType type = this.codeLookupTable.get(code);
        if (type == null) {
            return UNKNOWN;
        }
        return type;
    }

    @Override
    public ProxyRequestType unknown() {
        return UNKNOWN;
    }
}