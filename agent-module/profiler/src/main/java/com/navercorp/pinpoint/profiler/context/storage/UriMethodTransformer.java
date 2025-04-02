package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.common.util.StringUtils;

public class UriMethodTransformer implements UriTransformer {
    @Override
    public String transform(String httpMethod, String uri) {
        if (StringUtils.isEmpty(httpMethod)) {
            return uri;
        }
        return httpMethod + " " + uri;
    }
}
