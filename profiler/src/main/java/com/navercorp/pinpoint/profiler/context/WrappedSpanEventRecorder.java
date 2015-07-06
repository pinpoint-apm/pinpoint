package com.navercorp.pinpoint.profiler.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ParsingResult;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;

public class WrappedSpanEventRecorder extends AbstractRecorder implements SpanEventRecorder {
    private final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private SpanEvent spanEvent;

    public WrappedSpanEventRecorder(final TraceContext traceContext) {
        super(traceContext);
    }

    public void setSpanEvent(final SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
    }

    public SpanEvent getSpanEvent() {
        return spanEvent;
    }

    @Override
    public ParsingResult recordSqlInfo(String sql) {
        if (sql == null) {
            return null;
        }
        ParsingResult parsingResult = traceContext.parseSql(sql);
        recordSqlParsingResult(parsingResult);
        return parsingResult;
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
        recordSqlParsingResult(parsingResult, null);
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {
        if (parsingResult == null) {
            return;
        }
        final boolean isNewCache = traceContext.cacheSql(parsingResult);
        if (isDebug) {
            if (isNewCache) {
                logger.debug("update sql cache. parsingResult:{}", parsingResult);
            } else {
                logger.debug("cache hit. parsingResult:{}", parsingResult);
            }
        }

        final TIntStringStringValue tSqlValue = new TIntStringStringValue(parsingResult.getId());
        final String output = parsingResult.getOutput();
        if (isNotEmpty(output)) {
            tSqlValue.setStringValue1(output);
        }
        if (isNotEmpty(bindValue)) {
            tSqlValue.setStringValue2(bindValue);
        }
        recordSqlParam(tSqlValue);
    }

    private static boolean isNotEmpty(final String bindValue) {
        return bindValue != null && !bindValue.isEmpty();
    }

    private void recordSqlParam(TIntStringStringValue tIntStringStringValue) {
        spanEvent.addAnnotation(new Annotation(AnnotationKey.SQL_ID.getCode(), tIntStringStringValue));
    }

    @Override
    public void recordDestinationId(String destinationId) {
        spanEvent.setDestinationId(destinationId);
    }

    @Override
    public void recordNextSpanId(long nextSpanId) {
        if (nextSpanId == -1) {
            return;
        }
        spanEvent.setNextSpanId(nextSpanId);
    }

    @Override
    public void recordAsyncId(int asyncId) {
        spanEvent.setAsyncId(asyncId);
    }

    @Override
    public void recordNextAsyncId(int nextAsyncId) {
        spanEvent.setNextAsyncId(nextAsyncId);
    }

    @Override
    public void recordAsyncSequence(short asyncSequence) {
        spanEvent.setAsyncSequence(asyncSequence);
    }

    @Override
    public void markBeforeTime() {
        spanEvent.markStartTime();
    }

    @Override
    public void markAfterTime() {
        spanEvent.markAfterTime();
    }

    @Override
    void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        spanEvent.setExceptionInfo(exceptionClassId, exceptionMessage);
        if (!spanEvent.getSpan().isSetErrCode()) {
            spanEvent.getSpan().setErrCode(1);
        }
    }

    void recordApiId(final int apiId) {
        spanEvent.setApiId(apiId);
    }

    void addAnnotation(Annotation annotation) {
        spanEvent.addAnnotation(annotation);
    }

    @Override
    public void recordServiceType(ServiceType serviceType) {
        spanEvent.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(String rpc) {
        spanEvent.setRpc(rpc);
    }

    @Override
    public void recordEndPoint(String endPoint) {
        spanEvent.setEndPoint(endPoint);
    }
}