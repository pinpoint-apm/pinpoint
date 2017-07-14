/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.service;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyRegistry;
import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.common.util.StaticFieldLookUp;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

import java.util.List;

/**
 * @author emeroad
 */
public class DefaultAnnotationKeyRegistryService implements AnnotationKeyRegistryService {

    private final CommonLogger logger;

    private final TraceMetadataLoaderService typeLoaderService;
    private final AnnotationKeyRegistry registry;

    public DefaultAnnotationKeyRegistryService() {
        this(new DefaultTraceMetadataLoaderService(), StdoutCommonLoggerFactory.INSTANCE);
    }


    public DefaultAnnotationKeyRegistryService(TraceMetadataLoaderService typeLoaderService, CommonLoggerFactory commonLogger) {
        if (typeLoaderService == null) {
            throw new NullPointerException("typeLoaderService must not be null");
        }
        if (commonLogger == null) {
            throw new NullPointerException("commonLogger must not be null");
        }
        this.logger = commonLogger.getLogger(DefaultAnnotationKeyRegistryService.class.getName());
        this.typeLoaderService = typeLoaderService;
        this.registry = buildAnnotationKeyRegistry();
    }

    private AnnotationKeyRegistry buildAnnotationKeyRegistry() {
        AnnotationKeyRegistry.Builder builder = new AnnotationKeyRegistry.Builder();

        StaticFieldLookUp<AnnotationKey> staticFieldLookUp = new StaticFieldLookUp<AnnotationKey>(AnnotationKey.class, AnnotationKey.class);
        List<AnnotationKey> lookup = staticFieldLookUp.lookup();
        for (AnnotationKey serviceType: lookup) {
            if (logger.isInfoEnabled()) {
                logger.info("add Default AnnotationKey:" + serviceType);
            }
            builder.addAnnotationKey(serviceType);
        }

        final List<AnnotationKey> types = typeLoaderService.getAnnotationKeys();
        for (AnnotationKey type : types) {
            if (logger.isInfoEnabled()) {
                logger.info("add Plugin AnnotationKey:" + type);
            }
            builder.addAnnotationKey(type);
        }

        return builder.build();
    }


    @Override
    public AnnotationKey findAnnotationKey(int annotationCode) {
        return this.registry.findAnnotationKey(annotationCode);
    }

    @Override
    public AnnotationKey findAnnotationKeyByName(String keyName) {
        return this.registry.findAnnotationKeyByName(keyName);

    }

    @Override
    public AnnotationKey findApiErrorCode(int annotationCode) {
        return this.registry.findApiErrorCode(annotationCode);
    }
}
