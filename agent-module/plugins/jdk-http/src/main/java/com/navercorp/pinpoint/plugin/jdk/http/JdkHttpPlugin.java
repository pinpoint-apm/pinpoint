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
package com.navercorp.pinpoint.plugin.jdk.http;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionGetHeaderFieldInterceptor;
import com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionGetInputStreamInterceptor;
import com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionInterceptor;
import com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionPlainConnect0Interceptor;
import com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpsURLConnectionImplInterceptor;

import java.security.ProtectionDomain;

/**
 * @author Jongho Moon
 * @author yjqg6666
 */
public class JdkHttpPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {

    private MatchableTransformTemplate transformTemplate;

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    public static final String INTERCEPT_HTTPS_CLASS_NAME = "sun.net.www.protocol.https.HttpsURLConnectionImpl";

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JdkHttpPluginConfig config = new JdkHttpPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        transformTemplate.transform("sun.net.www.protocol.http.HttpURLConnection", HttpURLConnectionTransform.class);
        transformTemplate.transform(INTERCEPT_HTTPS_CLASS_NAME, HttpsURLConnectionImplTransform.class);
    }

    public static class HttpURLConnectionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod connectMethod = target.getDeclaredMethod("connect");
            if (connectMethod != null) {
                connectMethod.addScopedInterceptor(HttpURLConnectionInterceptor.class, "HttpURLConnectionInterceptor");
            }
            final InstrumentMethod plainConnect0Method = target.getDeclaredMethod("plainConnect0");
            if (plainConnect0Method != null) {
                plainConnect0Method.addInterceptor(HttpURLConnectionPlainConnect0Interceptor.class);
            }
            final InstrumentMethod getHeaderFieldMethod = target.getDeclaredMethod("getHeaderField", "int");
            if (getHeaderFieldMethod != null) {
                getHeaderFieldMethod.addInterceptor(HttpURLConnectionGetHeaderFieldInterceptor.class);
            }
            final InstrumentMethod getInputStreamMethod = target.getDeclaredMethod("getInputStream");
            if (getInputStreamMethod != null) {
                getInputStreamMethod.addInterceptor(HttpURLConnectionGetInputStreamInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class HttpsURLConnectionImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod connectMethod = target.getDeclaredMethod("connect");
            if (connectMethod != null) {
                connectMethod.addScopedInterceptor(HttpsURLConnectionImplInterceptor.class, "HttpURLConnectionInterceptor");
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}