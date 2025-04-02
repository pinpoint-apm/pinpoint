package com.navercorp.pinpoint.profiler.context.storage;

public interface UriTransformer {

    String transform(String method, String uri);
}
