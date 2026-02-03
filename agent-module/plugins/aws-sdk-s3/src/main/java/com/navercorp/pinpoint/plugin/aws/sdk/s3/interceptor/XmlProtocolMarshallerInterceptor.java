/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.AwsSdkS3Constants;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.RequestBuilderGetter;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.URIGetter;
import software.amazon.awssdk.http.SdkHttpFullRequest;

import java.net.URI;

public class XmlProtocolMarshallerInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final ClientDatabaseRequestRecorder<ClientDatabaseRequestWrapper> clientRequestRecorder;

    public XmlProtocolMarshallerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;

        final ClientDatabaseRequestAdaptor<ClientDatabaseRequestWrapper> clientRequestAdaptor = ClientDatabaseRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientDatabaseRequestRecorder<>(clientRequestAdaptor);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            if (Boolean.FALSE == (target instanceof RequestBuilderGetter) || Boolean.FALSE == (target instanceof URIGetter)) {
                return;
            }

            final SdkHttpFullRequest.Builder builder = ((RequestBuilderGetter) target)._$PINPOINT$_getRequestBuilder();
            final URI uri = ((URIGetter) target)._$PINPOINT$_getURI();
            if (builder == null || uri == null) {
                return;
            }

            final boolean sampling = trace.canSampled();
            if (!sampling) {
                return;
            }

            final SpanEventRecorder recorder = trace.traceBlockBegin();
            // set remote trace
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            recorder.recordServiceType(AwsSdkS3Constants.AWS_SDK_S3);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SdkHttpFullRequest.Builder builder = ((RequestBuilderGetter) target)._$PINPOINT$_getRequestBuilder();
            final URI uri = ((URIGetter) target)._$PINPOINT$_getURI();
            if (builder == null || uri == null) {
                return;
            }

            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
            // Accessing httpRequest here not BEFORE() because it can cause side effect.
            final ClientDatabaseRequestWrapper clientRequest = new S3ClientDatabaseRequestWrapper(uri);
            this.clientRequestRecorder.record(recorder, clientRequest);
            final String url = getUrl(uri);
            if (url != null) {
                final String httpUrl = InterceptorUtils.getHttpUrl(url, Boolean.FALSE);
                recorder.recordAttribute(AnnotationKey.HTTP_URL, httpUrl);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    String getUrl(URI uri) {
        if (uri.isAbsolute()) {
            return uri.toString();
        }
        return HostAndPort.toHostAndPortString(uri.getHost(), uri.getPort());
    }
}