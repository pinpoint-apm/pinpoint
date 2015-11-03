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

import java.lang.reflect.Method;

import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class CreateBeanInstanceInterceptor extends AbstractSpringBeanCreationInterceptor implements AroundInterceptor1 {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    
    public CreateBeanInstanceInterceptor(Instrumentor instrumentContext, TransformCallback transformer, TargetBeanFilter filter) {
        super(instrumentContext, transformer, filter);
    }

    @IgnoreMethod
    @Override
    public void before(Object target, Object arg0) {

    }

    @Override
    public void after(Object target, Object beanNameObject, Object result, Throwable throwable) {
        try {
            if (result == null) {
                return;
            }
            if (!(beanNameObject instanceof String)) {
                logger.warn("invalid type:{}", beanNameObject);
                return;
            }
            final String beanName = (String) beanNameObject;

            Object bean;
            try {
                Method getter = result.getClass().getMethod("getWrappedInstance"); 
                bean = getter.invoke(result);
            } catch (Exception e) {
                logger.warn("Fail to get create bean instance", e);
                return;
            }
            
            processBean(beanName, bean);
        } catch (Throwable t) {
            logger.warn("Unexpected exception", t);
        }
    }
}
