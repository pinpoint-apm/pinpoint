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
package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author jaehong.kim
 */
public class NingAsyncHttpClientPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    public static final ServiceType ASYNC_HTTP_CLIENT = ServiceTypeFactory.of(9056, "ASYNC_HTTP_CLIENT", RECORD_STATISTICS);
    public static final ServiceType ASYNC_HTTP_CLIENT_INTERNAL = ServiceTypeFactory.of(9057, "ASYNC_HTTP_CLIENT_INTERNAL", "ASYNC_HTTP_CLIENT");

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final NingAsyncHttpClientPluginConfig config = new NingAsyncHttpClientPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("NingAsyncHttpClientPlugin disabled");
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Add NingAsyncHttpClientPlugin {}", config);
        }

        // 1.8.x ~ 1.9.x
        logger.debug("Add AsyncHttpClient(1.8.x ~ 1.9.x)");
        addAsyncHttpClientTransformer();

        // 2.x
        logger.debug("Add DefaultAsyncHttpClient(2.x ~");
        addDefaultAsyncHttpClientTransformer();
    }

    private void addAsyncHttpClientTransformer() {
        transformTemplate.transform("com.ning.http.client.AsyncHttpClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod executeRequestMethod = InstrumentUtils.findMethod(target, "executeRequest", "com.ning.http.client.Request", "com.ning.http.client.AsyncHandler");
            executeRequestMethod.addInterceptor("com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor.ExecuteRequestInterceptor");
            return target.toBytecode();
            }
        });
    }

    private void addDefaultAsyncHttpClientTransformer() {
        transformTemplate.transform("org.asynchttpclient.DefaultAsyncHttpClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final InstrumentMethod executeRequestMethod = InstrumentUtils.findMethod(target, "execute", "org.asynchttpclient.Request", "org.asynchttpclient.AsyncHandler");
                executeRequestMethod.addInterceptor("com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor.ExecuteInterceptor");
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}