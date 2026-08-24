/*
 * Copyright 2026 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.ktor.client.KtorClientContinuationConstructorInterceptor;
import com.navercorp.pinpoint.plugin.ktor.client.KtorClientContinuationInterceptor;
import com.navercorp.pinpoint.plugin.ktor.client.KtorClientSendInterceptor;
import com.navercorp.pinpoint.plugin.ktor.client.KtorClientTraceAccessor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.ConfigureRoutingFactoryInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.NettyApplicationCallHandlerInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.NettyHttp1HandlerHandleRequestInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.NettyHttp1HandlerPrepareCallFromRequestInterceptor;
import com.navercorp.pinpoint.plugin.ktor.interceptor.SuspendFunctionGunInterceptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KtorPluginTest {
    @BeforeAll
    static void registerServiceTypes() {
        // registrations come from this plugin's own type-provider.yml
        KtorTestServiceTypes.register();
    }

    private static final String DEFAULT_SENDER = "io.ktor.client.plugins.HttpSend$DefaultSender";
    private static final String DEFAULT_SENDER_CONTINUATION = "io.ktor.client.plugins.HttpSend$DefaultSender$execute$1";

    private final MatchableTransformTemplate transformTemplate = mock(MatchableTransformTemplate.class);
    private final ProfilerPluginSetupContext setupContext = mock(ProfilerPluginSetupContext.class);

    @Test
    void setupDisabledDoesNothing() {
        DefaultProfilerConfig profilerConfig = new DefaultProfilerConfig();
        profilerConfig.getProperties().setProperty("profiler.ktor.enable", "false");
        when(setupContext.getConfig()).thenReturn(profilerConfig);

        newKtorPlugin().setup(setupContext);

        verifyNoInteractions(transformTemplate);
    }

    @Test
    void setupRegistersServerAndClientTransforms() {
        when(setupContext.getConfig()).thenReturn(new DefaultProfilerConfig());
        when(setupContext.getConfiguredApplicationType()).thenReturn(ServiceType.UNDEFINED);

        newKtorPlugin().setup(setupContext);

        verify(transformTemplate).transform("io.ktor.server.netty.http1.NettyHttp1Handler", KtorPlugin.NettyHttp1HandlerTransform.class);
        verify(transformTemplate).transform("io.ktor.server.netty.http1.NettyHttp1ApplicationCall", KtorPlugin.NettyHttp1ApplicationCallTransform.class);
        verify(transformTemplate).transform("io.ktor.server.netty.NettyApplicationCallHandler", KtorPlugin.NettyApplicationCallHandlerTransform.class);
        verify(transformTemplate).transform("io.ktor.util.pipeline.SuspendFunctionGun", KtorPlugin.SuspendFunctionGunTransform.class);
        verify(transformTemplate).transform("io.ktor.server.routing.Route", KtorPlugin.RouteTransform.class);
        verify(transformTemplate).transform(DEFAULT_SENDER, KtorPlugin.KtorClientDefaultSenderTransform.class);
        verify(transformTemplate).transform(DEFAULT_SENDER_CONTINUATION, KtorPlugin.KtorClientDefaultSenderContinuationTransform.class);
    }

    @Test
    void setupSkipsClientTransformsWhenClientDisabled() {
        DefaultProfilerConfig profilerConfig = new DefaultProfilerConfig();
        profilerConfig.getProperties().setProperty("profiler.ktor.client.enable", "false");
        when(setupContext.getConfig()).thenReturn(profilerConfig);
        when(setupContext.getConfiguredApplicationType()).thenReturn(KtorConstants.KTOR);

        newKtorPlugin().setup(setupContext);

        verify(transformTemplate).transform("io.ktor.server.netty.http1.NettyHttp1Handler", KtorPlugin.NettyHttp1HandlerTransform.class);
        verify(transformTemplate, never()).transform(DEFAULT_SENDER, KtorPlugin.KtorClientDefaultSenderTransform.class);
        verify(transformTemplate, never()).transform(DEFAULT_SENDER_CONTINUATION, KtorPlugin.KtorClientDefaultSenderContinuationTransform.class);
    }

    @Test
    void setupSkipsRouteTransformWhenRetransformDisabled() {
        DefaultProfilerConfig profilerConfig = new DefaultProfilerConfig();
        profilerConfig.getProperties().setProperty("profiler.ktor.http.server.retransform.configure-routing", "false");
        when(setupContext.getConfig()).thenReturn(profilerConfig);
        when(setupContext.getConfiguredApplicationType()).thenReturn(KtorConstants.KTOR);

        newKtorPlugin().setup(setupContext);

        verify(transformTemplate, never()).transform("io.ktor.server.routing.Route", KtorPlugin.RouteTransform.class);
    }

    @Test
    void defaultSenderTransformAddsSendInterceptor() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        InstrumentMethod execute = mock(InstrumentMethod.class);
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), DEFAULT_SENDER, bytecode)).thenReturn(target);
        when(target.getDeclaredMethod("execute", "io.ktor.client.request.HttpRequestBuilder", "kotlin.coroutines.Continuation")).thenReturn(execute);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.KtorClientDefaultSenderTransform().doInTransform(instrumentor, getClass().getClassLoader(), DEFAULT_SENDER, null, null, bytecode);

        verify(execute).addInterceptor(KtorClientSendInterceptor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void defaultSenderTransformSkipsMissingExecute() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), DEFAULT_SENDER, bytecode)).thenReturn(target);
        when(target.getDeclaredMethod("execute", "io.ktor.client.request.HttpRequestBuilder", "kotlin.coroutines.Continuation")).thenReturn(null);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.KtorClientDefaultSenderTransform().doInTransform(instrumentor, getClass().getClassLoader(), DEFAULT_SENDER, null, null, bytecode);

        verify(target, never()).getDeclaredMethods(any());
        assertSame(bytecode, transformed);
    }

    @Test
    void continuationTransformAddsFieldAndInterceptors() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        InstrumentMethod constructor = mock(InstrumentMethod.class);
        InstrumentMethod invokeSuspend = mock(InstrumentMethod.class);
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), DEFAULT_SENDER_CONTINUATION, bytecode)).thenReturn(target);
        when(target.getConstructor("io.ktor.client.plugins.HttpSend$DefaultSender", "kotlin.coroutines.Continuation")).thenReturn(constructor);
        when(target.getDeclaredMethod("invokeSuspend", "java.lang.Object")).thenReturn(invokeSuspend);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.KtorClientDefaultSenderContinuationTransform().doInTransform(instrumentor, getClass().getClassLoader(), DEFAULT_SENDER_CONTINUATION, null, null, bytecode);

        verify(target).addField(KtorClientTraceAccessor.class);
        verify(constructor).addInterceptor(KtorClientContinuationConstructorInterceptor.class);
        verify(invokeSuspend).addInterceptor(KtorClientContinuationInterceptor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void continuationTransformSkipsMissingMembers() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), DEFAULT_SENDER_CONTINUATION, bytecode)).thenReturn(target);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.KtorClientDefaultSenderContinuationTransform().doInTransform(instrumentor, getClass().getClassLoader(), DEFAULT_SENDER_CONTINUATION, null, null, bytecode);

        verify(target).addField(KtorClientTraceAccessor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void nettyHttp1HandlerTransformAddsInterceptors() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        InstrumentMethod handleRequest = mock(InstrumentMethod.class);
        InstrumentMethod prepareCall = mock(InstrumentMethod.class);
        String className = "io.ktor.server.netty.http1.NettyHttp1Handler";
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), className, bytecode)).thenReturn(target);
        when(target.getDeclaredMethod("handleRequest", "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.HttpRequest")).thenReturn(handleRequest);
        when(target.getDeclaredMethod("prepareCallFromRequest", "io.netty.channel.ChannelHandlerContext", "io.netty.handler.codec.http.HttpRequest")).thenReturn(prepareCall);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.NettyHttp1HandlerTransform().doInTransform(instrumentor, getClass().getClassLoader(), className, null, null, bytecode);

        verify(target).addField(AsyncContextAccessor.class);
        verify(handleRequest).addInterceptor(NettyHttp1HandlerHandleRequestInterceptor.class);
        verify(prepareCall).addInterceptor(NettyHttp1HandlerPrepareCallFromRequestInterceptor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void nettyHttp1ApplicationCallTransformAddsAccessorField() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        String className = "io.ktor.server.netty.http1.NettyHttp1ApplicationCall";
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), className, bytecode)).thenReturn(target);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.NettyHttp1ApplicationCallTransform().doInTransform(instrumentor, getClass().getClassLoader(), className, null, null, bytecode);

        verify(target).addField(AsyncContextAccessor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void nettyApplicationCallHandlerTransformAddsGetterAndInterceptor() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        InstrumentMethod handleRequest = mock(InstrumentMethod.class);
        String className = "io.ktor.server.netty.NettyApplicationCallHandler";
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), className, bytecode)).thenReturn(target);
        List<InstrumentMethod> methods = Collections.singletonList(handleRequest);
        when(target.getDeclaredMethods(any(MethodFilter.class))).thenReturn(methods);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.NettyApplicationCallHandlerTransform().doInTransform(instrumentor, getClass().getClassLoader(), className, null, null, bytecode);

        verify(target).addGetter(CoroutineContextGetter.class, "coroutineContext");
        verify(handleRequest).addInterceptor(NettyApplicationCallHandlerInterceptor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void suspendFunctionGunTransformAddsLoopInterceptors() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        InstrumentMethod loopOne = mock(InstrumentMethod.class);
        InstrumentMethod loopTwo = mock(InstrumentMethod.class);
        String className = "io.ktor.util.pipeline.SuspendFunctionGun";
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), className, bytecode)).thenReturn(target);
        List<InstrumentMethod> methods = Arrays.asList(loopOne, loopTwo);
        when(target.getDeclaredMethods(any(MethodFilter.class))).thenReturn(methods);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.SuspendFunctionGunTransform().doInTransform(instrumentor, getClass().getClassLoader(), className, null, null, bytecode);

        verify(loopOne).addInterceptor(SuspendFunctionGunInterceptor.class);
        verify(loopTwo).addInterceptor(SuspendFunctionGunInterceptor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void routeTransformAddsHandleInterceptorWithTransformer() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        InstrumentMethod handle = mock(InstrumentMethod.class);
        String className = "io.ktor.server.routing.Route";
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), className, bytecode)).thenReturn(target);
        List<InstrumentMethod> methods = Collections.singletonList(handle);
        when(target.getDeclaredMethods(any(MethodFilter.class))).thenReturn(methods);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.RouteTransform().doInTransform(instrumentor, getClass().getClassLoader(), className, null, null, bytecode);

        verify(handle).addInterceptor(eq(ConfigureRoutingFactoryInterceptor.class), any(Object[].class));
        assertSame(bytecode, transformed);
    }

    @Test
    void nettyHttp1HandlerTransformSkipsMissingMethods() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        String className = "io.ktor.server.netty.http1.NettyHttp1Handler";
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), className, bytecode)).thenReturn(target);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.NettyHttp1HandlerTransform().doInTransform(instrumentor, getClass().getClassLoader(), className, null, null, bytecode);

        verify(target).addField(AsyncContextAccessor.class);
        assertSame(bytecode, transformed);
    }

    @Test
    void nettyApplicationCallHandlerTransformIgnoresNullMethodEntry() throws Exception {
        Instrumentor instrumentor = mock(Instrumentor.class);
        InstrumentClass target = mock(InstrumentClass.class);
        String className = "io.ktor.server.netty.NettyApplicationCallHandler";
        byte[] bytecode = new byte[0];
        when(instrumentor.getInstrumentClass(getClass().getClassLoader(), className, bytecode)).thenReturn(target);
        List<InstrumentMethod> methods = Collections.singletonList(null);
        when(target.getDeclaredMethods(any(MethodFilter.class))).thenReturn(methods);
        when(target.toBytecode()).thenReturn(bytecode);

        byte[] transformed = new KtorPlugin.NettyApplicationCallHandlerTransform().doInTransform(instrumentor, getClass().getClassLoader(), className, null, null, bytecode);

        verify(target).addGetter(CoroutineContextGetter.class, "coroutineContext");
        assertSame(bytecode, transformed);
    }

    private KtorPlugin newKtorPlugin() {
        KtorPlugin plugin = new KtorPlugin();
        plugin.setTransformTemplate(transformTemplate);
        return plugin;
    }
}
