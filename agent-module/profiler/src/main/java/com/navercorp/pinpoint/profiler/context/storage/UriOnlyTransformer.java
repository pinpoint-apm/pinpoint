package com.navercorp.pinpoint.profiler.context.storage;

public class UriOnlyTransformer implements UriTransformer {
    @Override
    public String transform(String httpMethod, String uri) {
        return uri;
    }
}
