package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class AsyncDefaultTrace extends DefaultTrace {
    // own logger so the async lifecycle logs keep this class as their category;
    // the parent declares a static one for its own messages
    private static final Logger logger = LogManager.getLogger(AsyncDefaultTrace.class);
    private final boolean isDebug = logger.isDebugEnabled();

    private final AsyncState asyncState;

    public AsyncDefaultTrace(Span span,
                             CallStack<SpanEvent> callStack,
                             Storage storage,
                             SpanRecorder spanRecorder,
                             WrappedSpanEventRecorder wrappedSpanEventRecorder,
                             AsyncState asyncState) {
        super(span, callStack, storage, spanRecorder, wrappedSpanEventRecorder, CloseListener.EMPTY);
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
