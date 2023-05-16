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

import static com.navercorp.pinpoint.common.util.VarArgs.va;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.spring.web.interceptor.RequestBodyObtainInterceptor;
import com.navercorp.pinpoint.plugin.spring.web.interceptor.ResponseBodyObtainInterceptor;


/**
 * @author Jongho Moon
 *
 */
public class SpringWebMvcPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        transformTemplate.transform("org.springframework.web.servlet.FrameworkServlet", FrameworkServletTransform.class);

        // =============================================================================================

        // 注册报文采集拦截器
        transformTemplate.transform("org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor", RequestBodyResolverTransform.class);
        transformTemplate.transform("org.springframework.web.method.support.InvocableHandlerMethod", ResponseBodyResolverTransform.class);

    
        // =============================================================================================
    }

    public static class FrameworkServletTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            InstrumentMethod doGet = target.getDeclaredMethod("doGet", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
            doGet.addInterceptor(BasicMethodInterceptor.class, va(SpringWebMvcConstants.SPRING_MVC));

            InstrumentMethod doPost = target.getDeclaredMethod("doPost", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
            doPost.addInterceptor(BasicMethodInterceptor.class, va(SpringWebMvcConstants.SPRING_MVC));

            return target.toBytecode();
        }
    }

    public static class RequestBodyResolverTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Add server metadata
            InstrumentMethod startInternalEditor = target.getDeclaredMethod("readWithMessageConverters",
                    "org.springframework.web.context.request.NativeWebRequest",
                    "org.springframework.core.MethodParameter",
                    "java.lang.reflect.Type");
            if (startInternalEditor != null) {
                startInternalEditor.addScopedInterceptor(RequestBodyObtainInterceptor.class, SpringWebMvcConstants.BODY_OBTAIN_SCOPE, ExecutionPolicy.ALWAYS);
            }
            return target.toBytecode();
        }
    }

    public static class ResponseBodyResolverTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            // Add server metadata
//            InstrumentMethod startInternalEditor = target.getDeclaredMethod("doInvoke",
//                    "java.lang.Object[]");

            InstrumentMethod startInternalEditor = target.getDeclaredMethod("invokeForRequest",
                    "org.springframework.web.context.request.NativeWebRequest",
                    "org.springframework.web.method.support.ModelAndViewContainer",
                    "java.lang.Object[]");


            if (startInternalEditor != null) {
                startInternalEditor.addScopedInterceptor(ResponseBodyObtainInterceptor.class, SpringWebMvcConstants.BODY_OBTAIN_SCOPE, ExecutionPolicy.ALWAYS);
            }
            return target.toBytecode();
        }
    }



    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
