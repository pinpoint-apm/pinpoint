package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;

/**
 * @author emeroad
 */
public interface TraceFactory {
    Trace currentTraceObject();

    Trace currentRpcTraceObject();

    Trace currentRawTraceObject();

    Trace disableSampling();

    // remote 에서 샘플링 대상으로 선정된 경우.
    Trace continueTraceObject(TraceId traceID);

    Trace newTraceObject();

    void detachTraceObject();
}
