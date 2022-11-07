package com.navercorp.pinpoint.bootstrap.plugin.uri;

import java.util.Collections;

public class BypassingUriExtractorService<T> implements UriExtractorService<T> {
    @Override
    public UriExtractor<T> getUriExtractor(UriExtractorProviderLocator uriExtractorProviderLocator) {
        BypassingUriExtractor<T> extractor = new BypassingUriExtractor<>();
        return new UriExtractorChain<>(Collections.singletonList(extractor));
    }

    @Override
    @Deprecated
    public UriExtractor<T> get(UriExtractorProviderLocator uriExtractorProviderLocator) {
        return getUriExtractor(uriExtractorProviderLocator);
    }
}
