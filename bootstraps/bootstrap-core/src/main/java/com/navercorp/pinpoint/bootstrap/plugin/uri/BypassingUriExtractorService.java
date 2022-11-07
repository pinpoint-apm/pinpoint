package com.navercorp.pinpoint.bootstrap.plugin.uri;

import java.util.ArrayList;
import java.util.List;

public class BypassingUriExtractorService implements UriExtractorService {
    @Override
    public UriExtractor getUriExtractor(UriExtractorProviderLocator uriExtractorProviderLocator) {
        List<UriExtractor<Object>> result = new ArrayList<>();
        BypassingUriExtractor extractor = new BypassingUriExtractor();
        result.add(extractor);
        return new UriExtractorChain<>(result);
    }

    @Override
    @Deprecated
    public UriExtractor get(UriExtractorProviderLocator uriExtractorProviderLocator) {
        return getUriExtractor(uriExtractorProviderLocator);
    }
}
