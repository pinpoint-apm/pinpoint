package com.navercorp.pinpoint.bootstrap.plugin.uri;

import com.navercorp.pinpoint.common.trace.UriExtractorType;

public class BypassingUriExtractor implements UriExtractor {
    public static final UriExtractorType TYPE = UriExtractorType.BYPASSING;

    @Override
    public UriExtractorType getExtractorType() {
        return TYPE;
    }

    @Override
    public String getUri(Object target, String rawUrl) {
        return rawUrl;
    }
}
