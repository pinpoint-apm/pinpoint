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
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansTargetScope;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

import java.util.Set;

/**
 * @author jaehong.kim
 */
public class ClassPathDefinitionScannerDoScanInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private final ClassLoader classLoader;
    private final Instrumentor instrumentor;
    private final TransformCallback transformer;
    private final TargetBeanFilter filter;

    public ClassPathDefinitionScannerDoScanInterceptor(final Instrumentor instrumentor, final ClassLoader classLoader, final TransformCallback transformer, final TargetBeanFilter filter) {
        this.classLoader = classLoader;
        this.instrumentor = instrumentor;
        this.transformer = transformer;
        this.filter = filter;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (result == null || throwable != null || !(result instanceof Set)) {
            return;
        }

        try {
            final Set<Object> set = (Set<Object>) result;
            for (Object o : set) {
                if (o instanceof BeanDefinitionHolder) {
                    final BeanDefinitionHolder beanDefinitionHolder = (BeanDefinitionHolder) o;
                    if (filter.isTarget(SpringBeansTargetScope.COMPONENT_SCAN, beanDefinitionHolder.getBeanName(), beanDefinitionHolder.getBeanDefinition())) {
                        final String className = beanDefinitionHolder.getBeanDefinition().getBeanClassName();
                        this.instrumentor.transform(this.classLoader, className, this.transformer);
                        this.filter.addTransformed(className);
                    }
                }
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        }
    }
}