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
package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansTargetScope;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 */
public abstract class AbstractSpringBeanCreationInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final Instrumentor instrumentor;
    private final TransformCallback transformer;
    private final TargetBeanFilter filter;

    protected AbstractSpringBeanCreationInterceptor(Instrumentor instrumentor, TransformCallback transformer, TargetBeanFilter filter) {
        this.instrumentor = instrumentor;
        this.transformer = transformer;
        this.filter = filter;
    }

    protected final void processBean(String beanName, Object bean) {
        if (beanName == null || bean == null) {
            return;
        }

        Class<?> clazz = bean.getClass();
        if (clazz == null) {
            return;
        }

        if (!filter.isTarget(SpringBeansTargetScope.POST_PROCESSOR, beanName, clazz)) {
            return;
        }

        // If you want to trace inherited methods, you have to retranform super classes, too.
        instrumentor.retransform(clazz, transformer);
        filter.addTransformed(clazz.getName());
        if (logger.isInfoEnabled()) {
            logger.info("Retransform {}", clazz.getName());
        }
    }
}