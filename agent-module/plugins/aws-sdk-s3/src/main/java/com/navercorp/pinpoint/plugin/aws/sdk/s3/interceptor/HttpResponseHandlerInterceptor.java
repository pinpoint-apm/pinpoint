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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.AwsSdkS3Constants;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.AwsSdkS3PluginConfig;
import software.amazon.awssdk.core.Response;
import software.amazon.awssdk.http.SdkHttpFullResponse;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class HttpResponseHandlerInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final boolean statusCode;
    private final boolean markError;

    public HttpResponseHandlerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        this.statusCode = AwsSdkS3PluginConfig.isStatusCode(traceContext.getProfilerConfig());
        this.markError = AwsSdkS3PluginConfig.isMarkError(traceContext.getProfilerConfig());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(AwsSdkS3Constants.AWS_SDK_S3_INTERNAL);
        if (result instanceof Response) {
            Response response = (Response) result;
            if (response.exception() != null) {
                recorder.recordException(markError, response.exception());
            }

            if (statusCode) {
                final SdkHttpFullResponse httpResponse = response.httpResponse();
                if (httpResponse != null) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("Status Code: ").append(httpResponse.statusCode());
                    if (response.response() instanceof PutObjectResponse) {
                        sb.append(", ETag: ").append(((PutObjectResponse) response.response()).eTag());
                    } else if (response.response() instanceof CopyObjectResponse) {
                        sb.append(", ETag: ").append(((CopyObjectResponse) response.response()).copyObjectResult().eTag());
                    } else if (response.response() instanceof CompleteMultipartUploadResponse) {
                        sb.append(", ETag: ").append(((CompleteMultipartUploadResponse) response.response()).eTag());
                    }
                    recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, sb.toString());
                }
            }
        }
    }
}
