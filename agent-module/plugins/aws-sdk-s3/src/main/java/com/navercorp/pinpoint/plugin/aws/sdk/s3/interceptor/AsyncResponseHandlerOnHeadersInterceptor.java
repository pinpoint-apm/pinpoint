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
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.AwsSdkS3Constants;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.AwsSdkS3PluginConfig;
import software.amazon.awssdk.http.SdkHttpResponse;

public class AsyncResponseHandlerOnHeadersInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final boolean statusCode;

    public AsyncResponseHandlerOnHeadersInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        this.statusCode = AwsSdkS3PluginConfig.isStatusCode(traceContext.getProfilerConfig());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(AwsSdkS3Constants.AWS_SDK_S3_INTERNAL);
        recorder.recordException(throwable);
        if (statusCode) {
            final SdkHttpResponse httpResponse = ArrayArgumentUtils.getArgument(args, 0, SdkHttpResponse.class);
            if (httpResponse != null) {
                final StringBuilder sb = new StringBuilder();
                sb.append("Status Code: ").append(httpResponse.statusCode());
                final String eTag = getETag(httpResponse);
                if (eTag != null) {
                    sb.append(", ETag: ").append(eTag);
                }
                recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, sb.toString());
            }
        }
    }

    String getETag(SdkHttpResponse httpResponse) {
        if (httpResponse == null) {
            return null;
        }
        return httpResponse.firstMatchingHeader("ETag").orElse(null);
    }
}