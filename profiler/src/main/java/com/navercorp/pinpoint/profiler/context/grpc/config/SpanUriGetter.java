package com.navercorp.pinpoint.profiler.context.grpc.config;

import com.navercorp.pinpoint.profiler.context.id.Shared;

public interface SpanUriGetter {
    String getCollectedUri(Shared shared);
}
