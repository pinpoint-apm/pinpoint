package com.navercorp.pinpoint.plugin.jdk.http;
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

import java.security.ProtectionDomain;

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

/**
 * 
 * @author Jongho Moon
 *
 */
public class JdkHttpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        transformTemplate.transform("sun.net.www.protocol.http.HttpURLConnection", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addGetter("com.navercorp.pinpoint.plugin.jdk.http.ConnectedGetter", "connected");

                if (target.hasField("connecting", "boolean")) {
                    target.addGetter("com.navercorp.pinpoint.plugin.jdk.http.ConnectingGetter", "connecting");
                }

                final InstrumentMethod connectMethod = InstrumentUtils.findMethod(target, "connect");
                connectMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionInterceptor", "HttpURLConnection");

                final InstrumentMethod getInputStreamMethod = InstrumentUtils.findMethod(target, "getInputStream");
                getInputStreamMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionInterceptor", "HttpURLConnection");

                final InstrumentMethod getOutputStreamMethod = InstrumentUtils.findMethod(target, "getOutputStream");
                getOutputStreamMethod.addScopedInterceptor("com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionInterceptor", "HttpURLConnection");

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
