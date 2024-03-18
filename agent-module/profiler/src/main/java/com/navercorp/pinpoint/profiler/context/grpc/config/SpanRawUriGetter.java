package com.navercorp.pinpoint.profiler.context.grpc.config;

import com.navercorp.pinpoint.profiler.context.id.Shared;

public class SpanRawUriGetter implements SpanUriGetter {
    @Override
    public String getCollectedUri(Shared shared) {
        return shared.getRpcName();
    }
}
