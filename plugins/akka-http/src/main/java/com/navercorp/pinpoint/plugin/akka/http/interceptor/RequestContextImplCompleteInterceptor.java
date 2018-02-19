package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import akka.http.scaladsl.marshalling.ToResponseMarshallable;
import akka.http.scaladsl.model.StatusCode;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConstants;
import scala.Tuple2;

public class RequestContextImplCompleteInterceptor extends AsyncContextSpanEventEndPointInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(RequestContextImplCompleteInterceptor.class);

    public RequestContextImplCompleteInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        if (args[0] instanceof ToResponseMarshallable) {
            if (((ToResponseMarshallable) args[0]).value() instanceof Tuple2) {
                StatusCode code = (StatusCode) ((Tuple2) ((ToResponseMarshallable) args[0]).value())._1();
                recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, code.intValue());
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);
        recorder.recordException(throwable);
    }
}
