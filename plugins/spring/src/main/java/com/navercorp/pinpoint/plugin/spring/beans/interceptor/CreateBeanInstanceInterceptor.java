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
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 * @author Taejin Koo
 */
public class CreateBeanInstanceInterceptor extends AbstractSpringBeanCreationInterceptor implements AroundInterceptor1 {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private volatile Method getWrappedInstanceMethod;

    public CreateBeanInstanceInterceptor(Instrumentor instrumentor, TransformCallback transformer, TargetBeanFilter filter) {
        super(instrumentor, transformer, filter);
    }

// #1375 Workaround java level Deadlock
// https://oss.navercorp.com/pinpoint/pinpoint-naver/issues/1375
//    @IgnoreMethod
    @Override
    public void before(Object target, Object arg0) {
    }

    @Override
    public void after(Object target, Object beanNameObject, Object result, Throwable throwable) {
        try {
            if (result == null || throwable != null) {
                return;
            }

            if (!(beanNameObject instanceof String)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("invalid type:{}", beanNameObject);
                }
                return;
            }
            final String beanName = (String) beanNameObject;

            try {
                final Method getWrappedInstanceMethod = getGetWrappedInstanceMethod(result);
                if (getWrappedInstanceMethod != null) {
                    final Object bean = getWrappedInstanceMethod.invoke(result);
                    processBean(beanName, bean);
                }
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Fail to get create bean instance", e);
                }
                return;
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpected exception", t);
            }
        }
    }

    private Method getGetWrappedInstanceMethod(Object object) throws NoSuchMethodException {
        if (getWrappedInstanceMethod != null) {
            return getWrappedInstanceMethod;
        }

        synchronized (this) {
            if (getWrappedInstanceMethod != null) {
                return getWrappedInstanceMethod;
            }

            final Class<?> aClass = object.getClass();
            final Method findedMethod = aClass.getMethod("getWrappedInstance");
            if (findedMethod != null) {
                getWrappedInstanceMethod = findedMethod;
                return getWrappedInstanceMethod;
            }
        }
        return null;
    }
}