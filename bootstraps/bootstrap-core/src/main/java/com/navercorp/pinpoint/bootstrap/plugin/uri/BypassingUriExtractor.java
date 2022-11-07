package com.navercorp.pinpoint.bootstrap.plugin.uri;

import com.navercorp.pinpoint.common.trace.UriExtractorType;

public class BypassingUriExtractor<T> implements UriExtractor<T> {
    public static final UriExtractorType TYPE = UriExtractorType.BYPASSING;

    @Override
    public UriExtractorType getExtractorType() {
        return TYPE;
    }

    @Override
    public String getUri(T target, String rawUrl) {
        return rawUrl;
    }
}
