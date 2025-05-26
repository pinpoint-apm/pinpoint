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
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestWrapperAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.util.InterceptorUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.AwsSdkS3Constants;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.RequestBuilderGetter;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.URIGetter;
import software.amazon.awssdk.http.SdkHttpFullRequest;

import java.net.URI;

public class XmlProtocolMarshallerInterceptor extends SpanEventBlockSimpleAroundInterceptorForPlugin {
    private final ClientDatabaseRequestRecorder<ClientDatabaseRequestWrapper> clientRequestRecorder;
    private final RequestTraceWriter<SdkHttpFullRequest.Builder> requestTraceWriter;

    public XmlProtocolMarshallerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);

        final ClientDatabaseRequestAdaptor<ClientDatabaseRequestWrapper> clientRequestAdaptor = ClientDatabaseRequestWrapperAdaptor.INSTANCE;
        this.clientRequestRecorder = new ClientDatabaseRequestRecorder<>(clientRequestAdaptor);
        final ClientHeaderAdaptor<SdkHttpFullRequest.Builder> clientHeaderAdaptor = new SdkHttpFullRequestHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public boolean checkBeforeTraceBlockBegin(Trace trace, Object target, Object[] args) {
        if (Boolean.FALSE == (target instanceof RequestBuilderGetter) || Boolean.FALSE == (target instanceof URIGetter)) {
            return false;
        }

        final SdkHttpFullRequest.Builder builder = ((RequestBuilderGetter) target)._$PINPOINT$_getRequestBuilder();
        if (builder == null) {
            return false;
        }

        if (requestTraceWriter.isNested(builder)) {
            return false;
        }

        if (Boolean.FALSE == trace.canSampled()) {
            this.requestTraceWriter.write(builder);
            return false;
        }

        return true;
    }

    @Override
    public void beforeTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args) {
        if (Boolean.FALSE == (target instanceof RequestBuilderGetter) || Boolean.FALSE == (target instanceof URIGetter)) {
            return;
        }

        final SdkHttpFullRequest.Builder builder = ((RequestBuilderGetter) target)._$PINPOINT$_getRequestBuilder();
        final URI uri = ((URIGetter) target)._$PINPOINT$_getURI();
        if (builder == null || uri == null) {
            return;
        }

        // set remote trace
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        final ClientDatabaseRequestWrapper clientRequest = new S3ClientDatabaseRequestWrapper(uri);
        this.requestTraceWriter.write(builder, nextId, clientRequest.getEndPoint());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordServiceType(AwsSdkS3Constants.AWS_SDK_S3);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        final SdkHttpFullRequest.Builder builder = ((RequestBuilderGetter) target)._$PINPOINT$_getRequestBuilder();
        final URI uri = ((URIGetter) target)._$PINPOINT$_getURI();
        if (builder == null || uri == null) {
            return;
        }

        // Accessing httpRequest here not BEFORE() because it can cause side effect.
        final ClientDatabaseRequestWrapper clientRequest = new S3ClientDatabaseRequestWrapper(uri);
        this.clientRequestRecorder.record(recorder, clientRequest);
        final String url = getUrl(uri);
        if (url != null) {
            final String httpUrl = InterceptorUtils.getHttpUrl(url, Boolean.FALSE);
            recorder.recordAttribute(AnnotationKey.HTTP_URL, httpUrl);
        }
    }

    String getUrl(URI uri) {
        if (uri.isAbsolute()) {
            return uri.toString();
        }
        return HostAndPort.toHostAndPortString(uri.getHost(), uri.getPort());
    }
}