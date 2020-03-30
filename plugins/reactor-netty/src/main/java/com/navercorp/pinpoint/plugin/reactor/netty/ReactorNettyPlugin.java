/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.netty;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.ChannelOperationsChannelMethodInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.ChannelOperationsInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpServerHandleHttpServerStateInterceptor;
import com.navercorp.pinpoint.plugin.reactor.netty.interceptor.HttpServerHandleStateInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class ReactorNettyPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ReactorNettyPluginConfig config = new ReactorNettyPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} version range=[0.8.2.RELEASE, 0.9.1.RELEASE], config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final ReactorNettyDetector detector = new ReactorNettyDetector(config.getBootstrapMains());
            if (detector.detect()) {
                logger.info("Detected application type : {}", ReactorNettyConstants.REACTOR_NETTY);
                if (!context.registerApplicationType(ReactorNettyConstants.REACTOR_NETTY)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), ReactorNettyConstants.REACTOR_NETTY);
                }
            }
        }

        // HTTP server
        transformTemplate.transform("reactor.netty.http.server.HttpServerHandle", HttpServerHandleTransform.class);
        transformTemplate.transform("reactor.netty.channel.ChannelOperations", ChannelOperationsTransform.class);
        transformTemplate.transform("reactor.netty.http.server.HttpServerOperations", HttpServerOperationsTransform.class);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class HttpServerHandleTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod method = target.getDeclaredMethod("onStateChange", "reactor.netty.Connection", "reactor.netty.ConnectionObserver$State");
            if (method != null) {
                if (instrumentor.exist(loader, "reactor.netty.http.server.HttpServerState")) {
                    // over reactor-netty.0.7.x
                    method.addInterceptor(HttpServerHandleHttpServerStateInterceptor.class);
                } else {
                    method.addInterceptor(HttpServerHandleStateInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class ChannelOperationsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // HTTP server end-point
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("terminate", "onInboundError", "onInboundCancel", "onTerminate", "dispose", "onComplete", "onError"))) {
                method.addInterceptor(ChannelOperationsInterceptor.class);
            }

            // HTTP server end-point(defense code for try ~ catch)
            final InstrumentMethod channelMethod = target.getDeclaredMethod("channel");
            if (channelMethod != null) {
                channelMethod.addInterceptor(ChannelOperationsChannelMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class HttpServerOperationsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }
    }
}
