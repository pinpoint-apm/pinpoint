package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContext;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.storage.Storage;

import java.util.Objects;

public class AsyncDefaultTrace extends DefaultTrace {
    private final AsyncState asyncState;

    public AsyncDefaultTrace(Span span,
                             CallStack<SpanEvent> callStack,
                             Storage storage,
                             SpanRecorder spanRecorder,
                             WrappedSpanEventRecorder wrappedSpanEventRecorder,
                             ExceptionContext exceptionContext,
                             AsyncState asyncState) {
        super(span, callStack, storage, spanRecorder, wrappedSpanEventRecorder, exceptionContext, CloseListener.EMPTY);
        this.asyncState = Objects.requireNonNull(asyncState, "asyncState");
    }

    @Override
    public void close() {
        if (isClosed()) {
            logger.debug("Already closed");
            return;
        }
        if (asyncState.await()) {
            // flush.
            super.flush();
            if (isDebug) {
                logger.debug("Await trace={}, asyncState={}", this, this.asyncState);
            }
        } else {
            // close.
            super.close();
            if (isDebug) {
                logger.debug("Close trace={}. asyncState={}", this, this.asyncState);
            }
        }
    }

    @Override
    public String toString() {
        return "AsyncDefaultTrace{" +
                "asyncState=" + asyncState +
                "} " + super.toString();
    }
}
