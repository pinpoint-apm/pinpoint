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

package com.navercorp.pinpoint.profiler.modifier.spring.beans.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.profiler.ProfilerException;
import com.navercorp.pinpoint.bootstrap.instrument.RetransformEventTrigger;
import com.navercorp.pinpoint.profiler.modifier.Modifier;
import com.navercorp.pinpoint.profiler.modifier.ModifierTransformAdaptor;

/**
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 */
public abstract class AbstractSpringBeanCreationInterceptor implements SimpleAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final RetransformEventTrigger retransformEventTrigger;
    private final Modifier modifier;
    private final TargetBeanFilter filter;
    
    protected AbstractSpringBeanCreationInterceptor(RetransformEventTrigger retransformEventTrigger, Modifier modifier, TargetBeanFilter filter) {
        this.retransformEventTrigger = retransformEventTrigger;
        this.modifier = modifier;
        this.filter = filter;
    }

    protected final void processBean(String beanName, Object bean) {
        if (bean == null) {
            return;
        }
        
        Class<?> clazz = bean.getClass();
        
        if (!filter.isTarget(beanName, clazz)) {
            return;
        }
        
        // If you want to trace inherited methods, you have to retranform super classes, too.
        
        try {
            ModifierTransformAdaptor transformAdaptor = new ModifierTransformAdaptor(modifier);
            retransformEventTrigger.retransform(clazz, transformAdaptor);

            if (logger.isInfoEnabled()) {
                logger.info("Retransform {}", clazz.getName());
            }
        } catch (ProfilerException e) {
            logger.warn("Fail to retransform: {}", clazz.getName(), e);
            return;
        }
        
        filter.addTransformed(clazz);
    }

    @Override
    public final void before(Object target, Object[] args) {
        // do nothing
    }
}
