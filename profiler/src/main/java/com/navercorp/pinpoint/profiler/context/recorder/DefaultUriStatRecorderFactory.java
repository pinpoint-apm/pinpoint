/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.recorder;

import com.navercorp.pinpoint.bootstrap.plugin.uri.DisabledUriStatRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractor;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorProviderLocator;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorService;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorderFactory;
import com.navercorp.pinpoint.common.util.Assert;

import com.google.inject.Provider;

/**
 * @author Taejin Koo
 */
public class DefaultUriStatRecorderFactory implements UriStatRecorderFactory {

    private final UriExtractorProviderLocator uriExtractorProviderLocator;

    public DefaultUriStatRecorderFactory(Provider<UriExtractorProviderLocator> uriExtractorProviderLocatorProvider) {
        Assert.requireNonNull(uriExtractorProviderLocatorProvider, "uriExtractorProviderLocatorProvider");

        UriExtractorProviderLocator uriExtractorProviderLocator = uriExtractorProviderLocatorProvider.get();
        this.uriExtractorProviderLocator = Assert.requireNonNull(uriExtractorProviderLocator, "uriExtractorProviderLocator");
    }

    @Override
    public <T> UriStatRecorder<T> create(UriExtractorService<T> uriExtractorService) {
        Assert.requireNonNull(uriExtractorService, "uriExtractorService");
        UriExtractor<T> uriExtractor = uriExtractorService.get(uriExtractorProviderLocator);

        if (uriExtractor == null) {
            return DisabledUriStatRecorder.create();
        } else {
            return new DefaultUriStatRecorder<T>(uriExtractor);
        }
    }

}
