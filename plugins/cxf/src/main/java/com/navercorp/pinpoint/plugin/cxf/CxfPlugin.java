/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author barney
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/10/03
 */
public class CxfPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        CxfPluginConfig config = new CxfPluginConfig(context.getConfig());

        if (config.isServiceProfile()) {
            addCxfService();
        }

        if (config.isLoggingProfile()) {
            addCxfLogging();
        }

        if (config.isClientProfile()) {
            addCxfClient();
        }
    }

    private void addCxfService() {

        // cxf service invoker interceptor
        transformTemplate.transform("org.apache.cxf.interceptor.ServiceInvokerInterceptor", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                // handleMessageMethod
                InstrumentMethod handleMessageMethod = InstrumentUtils.findMethod(target, "handleMessage", new String[]{"org.apache.cxf.message.Message"});
                handleMessageMethod.addInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", va(CxfPluginConstants.CXF_SERVICE_INVOKER_SERVICE_TYPE));

                return target.toBytecode();
            }
        });

        // cxf message sender interceptor
        transformTemplate.transform("org.apache.cxf.interceptor.MessageSenderInterceptor", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                // handleMessageMethod
                InstrumentMethod handleMessageMethod = InstrumentUtils.findMethod(target, "handleMessage", new String[]{"org.apache.cxf.message.Message"});
                handleMessageMethod.addInterceptor("com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor", va(CxfPluginConstants.CXF_MESSAGE_SENDER_SERVICE_TYPE));

                return target.toBytecode();
            }
        });

    }

    private void addCxfLogging() {

        // cxf logging in interceptor
        transformTemplate.transform("org.apache.cxf.interceptor.LoggingInInterceptor", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // formatLoggingMessage
                InstrumentMethod formatLoggingMessage = InstrumentUtils.findMethod(target, "formatLoggingMessage", new String[]{"org.apache.cxf.interceptor.LoggingMessage"});
                formatLoggingMessage.addScopedInterceptor("com.navercorp.pinpoint.plugin.cxf.interceptor.CxfLoggingInMessageMethodInterceptor", CxfPluginConstants.CXF_SCOPE);

                return target.toBytecode();
            }
        });

        // cxf logging out interceptor
        transformTemplate.transform("org.apache.cxf.interceptor.LoggingOutInterceptor", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {

                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // formatLoggingMessage
                InstrumentMethod formatLoggingMessage = InstrumentUtils.findMethod(target, "formatLoggingMessage", new String[]{"org.apache.cxf.interceptor.LoggingMessage"});
                formatLoggingMessage.addScopedInterceptor("com.navercorp.pinpoint.plugin.cxf.interceptor.CxfLoggingOutMessageMethodInterceptor", CxfPluginConstants.CXF_SCOPE);

                return target.toBytecode();
            }
        });
    }


    @Deprecated
    private void addCxfClient() {

        // cxf ws client
        transformTemplate.transform("org.apache.cxf.frontend.ClientProxy", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // invokeSyncMethod
                InstrumentMethod invokeSyncMethod = InstrumentUtils.findMethod(target, "invokeSync", new String[]{"java.lang.reflect.Method", "org.apache.cxf.service.model.BindingOperationInfo", "java.lang.Object[]"});
                invokeSyncMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.cxf.interceptor.CxfClientInvokeSyncMethodInterceptor", CxfPluginConstants.CXF_CLIENT_SCOPE);

                return target.toBytecode();
            }
        });

        // cxf rs client
        transformTemplate.transform("org.apache.cxf.interceptor.MessageSenderInterceptor$MessageSenderEndingInterceptor", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className,
                                        Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                    throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // handleMessageMethod
                InstrumentMethod handleMessageMethod = InstrumentUtils.findMethod(target, "handleMessage", new String[]{"org.apache.cxf.message.Message"});
                handleMessageMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.cxf.interceptor.CxfClientHandleMessageMethodInterceptor", CxfPluginConstants.CXF_CLIENT_SCOPE);

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}