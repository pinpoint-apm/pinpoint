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

package com.navercorp.pinpoint.plugin.aws.sdk.s3;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.MatchableTransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor.AsyncResponseHandlerOnErrorInterceptor;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor.AsyncResponseHandlerOnHeadersInterceptor;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor.BaseClientHandlerInterceptor;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor.HttpResponseHandlerInterceptor;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor.S3ClientInterceptor;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor.XmlProtocolMarshallerInterceptor;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

public class AwsSdkS3Plugin implements ProfilerPlugin, MatchableTransformTemplateAware {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private MatchableTransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final AwsSdkS3PluginConfig config = new AwsSdkS3PluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("Disable {}", this.getClass().getSimpleName());
            return;
        }

        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        // for reactor
        addS3Client();
    }

    private void addS3Client() {
        transformTemplate.transform("software.amazon.awssdk.services.s3.DefaultS3Client", S3ClientTransform.class);
        final Matcher httpResponseHandlerMatcher = Matchers.newPackageBasedMatcher("software.amazon.awssdk", new InterfaceInternalNameMatcherOperand("software.amazon.awssdk.core.http.HttpResponseHandler", true));
        transformTemplate.transform(httpResponseHandlerMatcher, HttpResponseHandlerTransform.class);
        final Matcher asyncHttpResponseHandlerMatcher = Matchers.newPackageBasedMatcher("software.amazon.awssdk", new InterfaceInternalNameMatcherOperand("software.amazon.awssdk.core.internal.http.TransformingAsyncResponseHandler", true));
        transformTemplate.transform(asyncHttpResponseHandlerMatcher, AsyncResponseHandlerTransform.class);
        transformTemplate.transform("software.amazon.awssdk.core.internal.handler.BaseSyncClientHandler", BaseSyncClientHandlerTransform.class);
        transformTemplate.transform("software.amazon.awssdk.core.internal.handler.BaseAsyncClientHandler", BaseAsyncClientHandlerTransform.class);
        transformTemplate.transform("software.amazon.awssdk.protocols.xml.internal.marshall.XmlProtocolMarshaller", XmlProtocolMarshallerTransform.class);
    }

    public static class S3ClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final MethodFilter nameExcludeMethodFilter = MethodFilters.nameExclude(
                    "utilities",
                    "waiter",
                    "serviceName",
                    "serviceClientConfiguration",
                    "close"
            );
            final MethodFilter accessFlagMethodFilter = MethodFilters.modifier(Modifier.PUBLIC, Modifier.ABSTRACT | Modifier.NATIVE | Modifier.STATIC);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(nameExcludeMethodFilter, accessFlagMethodFilter))) {
                method.addInterceptor(S3ClientInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class HttpResponseHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod handleMethod = target.getDeclaredMethod("handle", "software.amazon.awssdk.http.SdkHttpFullResponse", "software.amazon.awssdk.core.interceptor.ExecutionAttributes");
            if (handleMethod != null) {
                handleMethod.addInterceptor(HttpResponseHandlerInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class AsyncResponseHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod onHeadersMethod = target.getDeclaredMethod("onHeaders", "software.amazon.awssdk.http.SdkHttpResponse");
            if (onHeadersMethod != null) {
                onHeadersMethod.addInterceptor(AsyncResponseHandlerOnHeadersInterceptor.class);
            }
            final InstrumentMethod onErrorMethod = target.getDeclaredMethod("onError", "java.lang.Throwable");
            if (onErrorMethod != null) {
                onErrorMethod.addInterceptor(AsyncResponseHandlerOnErrorInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class BaseSyncClientHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod invokeMethod = target.getDeclaredMethod("doExecute", "software.amazon.awssdk.core.client.handler.ClientExecutionParams", "software.amazon.awssdk.core.http.ExecutionContext", "software.amazon.awssdk.core.http.HttpResponseHandler");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(BaseClientHandlerInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class BaseAsyncClientHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final InstrumentMethod invokeMethod = target.getDeclaredMethod("doExecute", "software.amazon.awssdk.core.client.handler.ClientExecutionParams", "software.amazon.awssdk.core.http.ExecutionContext", "software.amazon.awssdk.core.internal.http.TransformingAsyncResponseHandler");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(BaseClientHandlerInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class XmlProtocolMarshallerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (target.hasField("request")) {
                target.addGetter(RequestBuilderGetter.class, "request");
            }
            if (target.hasField("endpoint")) {
                target.addGetter(URIGetter.class, "endpoint");
            }

            final InstrumentMethod invokeMethod = target.getDeclaredMethod("finishMarshalling", "software.amazon.awssdk.core.SdkPojo");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(XmlProtocolMarshallerInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(MatchableTransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}