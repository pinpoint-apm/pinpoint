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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.TypeProviderLoader;
import com.navercorp.pinpoint.common.util.DefaultDisplayArgument;
import com.navercorp.pinpoint.common.util.DisplayArgumentMatcher;
import com.navercorp.pinpoint.common.util.StaticFieldLookUp;
import com.navercorp.pinpoint.web.servlet.ServiceTypeLoader;
import com.navercorp.pinpoint.web.util.AnnotationKeyMatcherRegistry;
import com.navercorp.pinpoint.common.plugin.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author emeroad
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class DefaultAnnotationKeyMatcherService implements AnnotationKeyMatcherService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AnnotationKeyMatcherRegistry registry;

    public DefaultAnnotationKeyMatcherService() {

        AnnotationKeyMatcherRegistry.Builder builder = new AnnotationKeyMatcherRegistry.Builder();

        StaticFieldLookUp<DisplayArgumentMatcher> staticFieldLookUp = new StaticFieldLookUp<DisplayArgumentMatcher>(DefaultDisplayArgument.class, DisplayArgumentMatcher.class);
        List<DisplayArgumentMatcher> lookup = staticFieldLookUp.lookup();
        for (DisplayArgumentMatcher displayArgumentMatcher : lookup) {
            AnnotationKeyMatcher annotationKeyMatcher = displayArgumentMatcher.getAnnotationKeyMatcher();
            if (annotationKeyMatcher == null) {
                continue;
            }
            logger.debug("add DefaultAnnotationKeyMatcher ServiceType:{}, AnnotationKeyMatcher:{}", displayArgumentMatcher.getServiceType(), annotationKeyMatcher);
            builder.addAnnotationMatcher(displayArgumentMatcher.getServiceType(), annotationKeyMatcher);
        }

        List<Type> types = loadType();
        for (Type type : types) {
            if (type.getAnnotationKeyMatcher() == null) {
                continue;
            }
            logger.debug("add AnnotationKeyMatcher ServiceType:{}, AnnotationKeyMatcher:{}", type.getServiceType(), type.getAnnotationKeyMatcher());
            builder.addAnnotationMatcher(type.getServiceType(), type.getAnnotationKeyMatcher());
        }
        this.registry = builder.build();
    }

    private List<Type> loadType() {
        final TypeProviderLoader typeProviderLoader = ServiceTypeLoader.getTypeProviderLoader();
        return typeProviderLoader.getTypes();
    }

    @Override
    public AnnotationKeyMatcher findAnnotationKeyMatcher(short serviceType) {
        return registry.findAnnotationKeyMatcher(serviceType);
    }
}
