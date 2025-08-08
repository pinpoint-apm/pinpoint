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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.aws.sdk.s3.AwsSdkS3Constants;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.core.client.handler.ClientExecutionParams;

public class BaseClientHandlerInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    public BaseClientHandlerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        recorder.recordServiceType(AwsSdkS3Constants.AWS_SDK_S3_INTERNAL);
        final ClientExecutionParams clientExecutionParams = ArrayArgumentUtils.getArgument(args, 0, ClientExecutionParams.class);
        if (clientExecutionParams == null) {
            return;
        }
        final SdkRequest sdkRequest = clientExecutionParams.getInput();
        if (sdkRequest != null) {
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, sdkRequest.toString());
        }
        AsyncContext asyncContext = recorder.recordNextAsyncContext();
        AsyncContextAccessorUtils.setAsyncContext(asyncContext, args, 2);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}
