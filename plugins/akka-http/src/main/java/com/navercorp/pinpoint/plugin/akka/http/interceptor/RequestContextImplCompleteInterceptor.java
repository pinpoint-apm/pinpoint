/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import akka.http.javadsl.server.Complete;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConstants;
import scala.Option;
import scala.concurrent.Future;
import scala.util.Failure;
import scala.util.Success;

public class RequestContextImplCompleteInterceptor extends AsyncContextSpanEventEndPointInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(RequestContextImplCompleteInterceptor.class);

    public RequestContextImplCompleteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        try {
            if (result instanceof Future && ((Future) result).isCompleted()) {
                Option value = ((Future) result).value();
                if (value == null) {
                    return;
                }

                Object routeResult = value.get();
                if (routeResult instanceof Success) {
                    Object success = ((Success) routeResult).get();
                    if (success instanceof Complete) {
                        akka.http.javadsl.model.HttpResponse response = ((Complete) success).getResponse();
                        if (response == null) {
                            return;
                        }
                        akka.http.javadsl.model.StatusCode status = response.status();
                        if (status == null) {
                            return;
                        }
                        recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, status.intValue());
                    }
                } else if (routeResult instanceof Failure) {
                    Throwable failure = ((Failure) routeResult).exception();
                    recorder.recordException(failure);
                }
            }
        } finally {
            recorder.recordApi(methodDescriptor);
            recorder.recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);
            recorder.recordException(throwable);
        }
    }

}
