package com.navercorp.pinpoint.profiler.context.grpc.config;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface SpanUriGetter {
    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface ToCollectedUri {
    }

    String DEFAULT_RPC_NAME = "UNKNOWN";

    @ToCollectedUri
    default String getNonEmptyCollectedUri(Shared shared){
        String collectedUri = getCollectedUri(shared);
        if (StringUtils.isEmpty(collectedUri)) {
            return DEFAULT_RPC_NAME;
        }
        return collectedUri;
    }

    String getCollectedUri(Shared shared);
}
