/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ktor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.ktor.interceptor.ConfigureRoutingFactoryInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.NettyApplicationCallHandlerInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.NettyHttp1HandlerHandleRequestInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.NettyHttp1HandlerPrepareCallFromRequestInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.SuspendFunctionGunInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class KtorPlugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        KtorPluginConfig config = new KtorPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final KtorDetector detector = new KtorDetector(config.getBootstrapMains());
            if (detector.detect()) {
                logger.info("Detected application type : {}", KtorConstants.KTOR);
                if (!context.registerApplicationType(KtorConstants.KTOR)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), KtorConstants.KTOR);
                }
            }
        }

        // Server
        transformTemplate.transform("io.ktor.server.netty.http1.NettyHttp1Handler", NettyHttp1HandlerTransform.class);
        transformTemplate.transform("io.ktor.server.netty.http1.NettyHttp1ApplicationCall", NettyHttp1ApplicationCallTransform.class);
        transformTemplate.transform("io.ktor.server.netty.NettyApplicationCallHandler", NettyApplicationCallHandlerTransform.class);
        transformTemplate.transform("io.ktor.util.pipeline.SuspendFunctionGun", SuspendFunctionGunTransform.class);
        if (config.isRetransformConfigureRouting()) {
            transformTemplate.transform("io.ktor.server.routing.Route", RouteTransform.class);
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class NettyHttp1HandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            InstrumentMethod handleRequestMethod = target.getDeclaredMethod("handleRequest", "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.HttpRequest");
            if (handleRequestMethod != null) {
                handleRequestMethod.addInterceptor(NettyHttp1HandlerHandleRequestInterceptor.class);
            }

            InstrumentMethod prepareCallFromRequestMethod = target.getDeclaredMethod("prepareCallFromRequest", "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.HttpRequest");
            if (prepareCallFromRequestMethod != null) {
                prepareCallFromRequestMethod.addInterceptor(NettyHttp1HandlerPrepareCallFromRequestInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class NettyHttp1ApplicationCallTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }
    }

    public static class NettyApplicationCallHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addGetter(CoroutineContextGetter.class, "coroutineContext");

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("handleRequest"))) {
                if (method != null) {
                    method.addInterceptor(NettyApplicationCallHandlerInterceptor.class);
                }
            }

            return target.toBytecode();
        }
    }

    public static class SuspendFunctionGunTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("loop"))) {
                method.addInterceptor(SuspendFunctionGunInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class RouteTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final RouteMethodTransformer routeMethodTransformer = new RouteMethodTransformer(Boolean.FALSE);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("handle"))) {
                method.addInterceptor(ConfigureRoutingFactoryInterceptor.class, va(routeMethodTransformer));
            }

            return target.toBytecode();
        }
    }
}
