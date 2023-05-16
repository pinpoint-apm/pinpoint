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
package com.navercorp.pinpoint.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.spring.web.interceptor.ExposePathWithinMappingInterceptor;
import com.navercorp.pinpoint.plugin.spring.web.interceptor.InvocableHandlerMethodInvokeForRequestMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.web.javax.interceptor.LookupHandlerMethodInterceptor;
import com.navercorp.pinpoint.plugin.spring.web.javax.interceptor.ProcessRequestInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class SpringWebMvcPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        SpringWebMvcConfig config = new SpringWebMvcConfig(context.getConfig());
        if (Boolean.FALSE == config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }

        transformTemplate.transform("org.springframework.web.servlet.FrameworkServlet", FrameworkServletTransform.class, new Object[]{config.isUriStatEnable(), config.isUriStatUseUserInput()}, new Class[]{Boolean.class, Boolean.class});

        // Async
        transformTemplate.transform("org.springframework.web.method.support.InvocableHandlerMethod", InvocableHandlerMethodTransform.class);

        // uri stat
        if (config.isUriStatEnable()) {
            transformTemplate.transform("org.springframework.web.servlet.handler.AbstractHandlerMethodMapping", AbstractHandlerMethodMappingTransform.class);
            transformTemplate.transform("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping", AbstractUrlHandlerMappingTransform.class);
        }

    }

    public static class AbstractHandlerMethodMappingTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // Add attribute listener.
            final InstrumentMethod lookupHandlerMethod = target.getDeclaredMethod("lookupHandlerMethod", "java.lang.String", "javax.servlet.http.HttpServletRequest");
            if (lookupHandlerMethod != null) {
                lookupHandlerMethod.addInterceptor(LookupHandlerMethodInterceptor.class);
            }

            // Spring 6
            final InstrumentMethod lookupHandlerMethodJakarta = target.getDeclaredMethod("lookupHandlerMethod", "java.lang.String", "jakarta.servlet.http.HttpServletRequest");
            if (lookupHandlerMethodJakarta != null) {
                lookupHandlerMethodJakarta.addInterceptor(com.navercorp.pinpoint.plugin.spring.web.jakarta.interceptor.LookupHandlerMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class AbstractUrlHandlerMappingTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // Add attribute listener.
            final InstrumentMethod exposePathWithinMapping = target.getDeclaredMethod("exposePathWithinMapping", "java.lang.String", "java.lang.String", "javax.servlet.http.HttpServletRequest");
            if (exposePathWithinMapping != null) {
                exposePathWithinMapping.addInterceptor(ExposePathWithinMappingInterceptor.class);
            }

            // Spring 6
            final InstrumentMethod exposePathWithinMappingJakarta = target.getDeclaredMethod("exposePathWithinMapping", "java.lang.String", "java.lang.String", "jakarta.servlet.http.HttpServletRequest");
            if (exposePathWithinMappingJakarta != null) {
                exposePathWithinMappingJakarta.addInterceptor(ExposePathWithinMappingInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class FrameworkServletTransform implements TransformCallback {

        private final Boolean uriStatEnable;
        private final Boolean uriStatUseUserInput;

        public FrameworkServletTransform(Boolean uriStatEnable, Boolean uriStatUseUserInput) {
            this.uriStatEnable = uriStatEnable;
            this.uriStatUseUserInput = uriStatUseUserInput;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod doGet = target.getDeclaredMethod("doGet", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
            final InstrumentMethod doPost = target.getDeclaredMethod("doPost", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
            if (doGet != null) {
                doGet.addInterceptor(BasicMethodInterceptor.class, va(SpringWebMvcConstants.SPRING_MVC));
            }
            if (doPost != null) {
                doPost.addInterceptor(BasicMethodInterceptor.class, va(SpringWebMvcConstants.SPRING_MVC));
            }

            // Spring 6
            final InstrumentMethod doGetJakarta = target.getDeclaredMethod("doGet", "jakarta.servlet.http.HttpServletRequest", "jakarta.servlet.http.HttpServletResponse");
            final InstrumentMethod doPostJakarta = target.getDeclaredMethod("doPost", "jakarta.servlet.http.HttpServletRequest", "jakarta.servlet.http.HttpServletResponse");
            if (doGetJakarta != null) {
                doGetJakarta.addInterceptor(BasicMethodInterceptor.class, va(SpringWebMvcConstants.SPRING_MVC));
            }
            if (doPostJakarta != null) {
                doPostJakarta.addInterceptor(BasicMethodInterceptor.class, va(SpringWebMvcConstants.SPRING_MVC));
            }

            if (this.uriStatEnable) {
                final InstrumentMethod processRequest = target.getDeclaredMethod("processRequest", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
                if (processRequest != null) {
                    processRequest.addInterceptor(ProcessRequestInterceptor.class, va(this.uriStatUseUserInput));
                }

                // Spring 6
                final InstrumentMethod processRequestJakarta = target.getDeclaredMethod("processRequest", "jakarta.servlet.http.HttpServletRequest", "jakarta.servlet.http.HttpServletResponse");
                if (processRequestJakarta != null) {
                    processRequestJakarta.addInterceptor(com.navercorp.pinpoint.plugin.spring.web.jakarta.interceptor.ProcessRequestInterceptor.class, va(this.uriStatUseUserInput));
                }
            }

            return target.toBytecode();
        }
    }

    public static class InvocableHandlerMethodTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeForRequestMethod = target.getDeclaredMethod("invokeForRequest", "org.springframework.web.context.request.NativeWebRequest", "org.springframework.web.method.support.ModelAndViewContainer", "java.lang.Object[]");
            if (invokeForRequestMethod != null) {
                invokeForRequestMethod.addInterceptor(InvocableHandlerMethodInvokeForRequestMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
