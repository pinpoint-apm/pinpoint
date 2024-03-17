package com.navercorp.pinpoint.profiler.context.grpc.config;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.id.Shared;

public class SpanAutoUriGetter implements SpanUriGetter {
    @Override
    public String getCollectedUri(Shared shared) {
        String uriTemplate = shared.getUriTemplate();
        if (StringUtils.isEmpty(uriTemplate)) {
            return shared.getRpcName();
        }
        return uriTemplate;
    }
}
