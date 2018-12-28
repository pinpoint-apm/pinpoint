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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.TraceMetadataLoader;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.plugins.loader.trace.TraceMetadataProviderLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServerTraceMetadataLoaderService implements TraceMetadataLoaderService {

    // External plugin type providers
    private static final String DEFAULT_TYPE_PROVIDER_PATH = "classpath*:META-INF/pinpoint/type-providers/*.yml";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TraceMetadataLoader loader;

    public ServerTraceMetadataLoaderService(CommonLoggerFactory commonLoggerFactory) {
        this(ClassUtils.getDefaultClassLoader(), Collections.singletonList(DEFAULT_TYPE_PROVIDER_PATH), commonLoggerFactory);
    }

    public ServerTraceMetadataLoaderService(ClassLoader classLoader, Collection<String> typeProviderPaths, CommonLoggerFactory commonLoggerFactory) {
        Assert.requireNonNull(classLoader, "classLoader must not be null");
        Assert.requireNonNull(typeProviderPaths, "typeProviderPaths must not be null");
        Assert.requireNonNull(commonLoggerFactory, "commonLoggerFactory must not be null");

        logger.info("Loading additional type providers using : {}", typeProviderPaths);
        List<URL> typeProviderUrls = getTypeProviderUrls(classLoader, typeProviderPaths);
        logger.info("Additional type providers : {}", typeProviderUrls);

        TraceMetadataProviderLoader traceMetadataProviderLoader = new TraceMetadataProviderLoader(typeProviderUrls);
        List<TraceMetadataProvider> traceMetadataProviderList = traceMetadataProviderLoader.load(classLoader);

        this.loader = new TraceMetadataLoader(commonLoggerFactory);
        loader.load(traceMetadataProviderList);
    }

    private List<URL> getTypeProviderUrls(ClassLoader classLoader, Collection<String> typeProviderPaths) {
        ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver(classLoader);
        List<URL> typeProviderUrls = new ArrayList<>();
        for (String typeProviderPath : typeProviderPaths) {
            try {
                Resource[] resources = resourceLoader.getResources(typeProviderPath);
                if (ArrayUtils.isEmpty(resources)) {
                    logger.info("{} did not match any resources.", typeProviderPath);
                } else {
                    Arrays.stream(resources)
                            .map(this::toUrl)
                            .forEach(typeProviderUrls::add);
                }
            } catch (IOException e) {
                logger.error("Error getting resources using " + typeProviderPath + ", skipping.", e);
            }
        }
        return typeProviderUrls;
    }

    private URL toUrl(Resource resource) {
        try {
            return resource.getURL();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to get url from " + resource.getDescription(), e);
        }
    }

    @Override
    public List<ServiceTypeInfo> getServiceTypeInfos() {
        return loader.getServiceTypeInfos();
    }

    @Override
    public List<AnnotationKey> getAnnotationKeys() {
        return loader.getAnnotationKeys();
    }}
