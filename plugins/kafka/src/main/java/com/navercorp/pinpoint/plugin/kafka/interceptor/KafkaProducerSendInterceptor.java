package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;


public class KafkaProducerSendInterceptor implements AroundInterceptor {
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private static String CALL_SERVER;

    public KafkaProducerSendInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        KafkaConfig config = new KafkaConfig(traceContext.getProfilerConfig());
        CALL_SERVER = config.getCaller();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(KafkaConstants.KAFKA);
            doInBeforeTrace(recorder, trace, args);
        }

    }

    private void doInBeforeTrace(SpanEventRecorder recorder, Trace trace, Object args[]) {
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        ProducerRecord record = (ProducerRecord) args[0];
        record.headers().add(new RecordHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId().getBytes()));
        record.headers().add(new RecordHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()).getBytes()));
        record.headers().add(new RecordHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()).getBytes()));
        record.headers().add(new RecordHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()).getBytes()));
        record.headers().add(new RecordHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName().getBytes()));
        record.headers().add(new RecordHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()).getBytes()));
        recorder.recordEndPoint(CALL_SERVER);
        recorder.recordDestinationId(CALL_SERVER);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }
}
