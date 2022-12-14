package com.navercorp.pinpoint.bootstrap.plugin.uri;

import java.util.Collections;

public class BypassingUriExtractorService<T> implements UriExtractorService<T> {
    @Override
    public UriExtractor<T> get(UriExtractorProviderLocator locator) {
        BypassingUriExtractor<T> extractor = new BypassingUriExtractor<>();
        return new UriExtractorChain<>(Collections.singletonList(extractor));
    }

}
