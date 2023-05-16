package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import javax.annotation.Nullable;
import java.util.Objects;

public class DefaultCloseListener implements CloseListener {

    private final LocalTraceRoot traceRoot;
    @Nullable
    private final ActiveTraceHandle activeTraceHandle;
    @Nullable
    private final UriStatStorage uriStatStorage;

    public DefaultCloseListener(LocalTraceRoot traceRoot, ActiveTraceHandle activeTraceHandle, UriStatStorage uriStatStorage) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.activeTraceHandle = activeTraceHandle;
        this.uriStatStorage = uriStatStorage;
    }

    @Override
    public void close(long endTime) {
        recordUriTemplate(endTime);
        purgeActiveTrace(endTime);
    }


    private void recordUriTemplate(long afterTime) {
        final UriStatStorage copy = uriStatStorage;
        if (copy == null) {
            return;
        }

        Shared shared = traceRoot.getShared();
        String uriTemplate = shared.getUriTemplate();
        long traceStartTime = traceRoot.getTraceStartTime();

        boolean status = getStatus(shared.getErrorCode());
        copy.store(uriTemplate, status, traceStartTime, afterTime);
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
