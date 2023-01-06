package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import javax.annotation.Nullable;

public class DefaultCloseListener implements CloseListener {

    @Nullable
    private final ActiveTraceHandle activeTraceHandle;
    @Nullable
    private final UriStatStorage uriStatStorage;

    public DefaultCloseListener(ActiveTraceHandle activeTraceHandle, UriStatStorage uriStatStorage) {
        this.activeTraceHandle = activeTraceHandle;
        this.uriStatStorage = uriStatStorage;
    }

    @Override
    public void close(Span span) {
        final long end = span.getStartTime() + span.getElapsedTime();
        recordUriTemplate(span, end);
        purgeActiveTrace(end);
    }


    private void recordUriTemplate(Span span, long afterTime) {
        if (uriStatStorage == null) {
            return;
        }

        TraceRoot traceRoot = span.getTraceRoot();
        Shared shared = traceRoot.getShared();
        String uriTemplate = shared.getUriTemplate();
        long traceStartTime = traceRoot.getTraceStartTime();

        boolean status = getStatus(shared.getErrorCode());
        uriStatStorage.store(uriTemplate, status, traceStartTime, afterTime);
    }

    private boolean getStatus(int errorCode) {
        if (errorCode == 0) {
            return true;
        }
        return false;
    }

    private void purgeActiveTrace(long currentTime) {
        final ActiveTraceHandle copy = this.activeTraceHandle;
        if (copy != null) {
            copy.purge(currentTime);
        }
    }
}
