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
import java.util.Objects;
import com.navercorp.pinpoint.profiler.context.storage.UriStatStorage;

import com.google.inject.Provider;

/**
 * @author Taejin Koo
 */
public class DefaultUriStatRecorderFactory implements UriStatRecorderFactory {

    private final UriExtractorProviderLocator uriExtractorProviderLocator;
    private final UriStatStorage uriStatStorage;

    public DefaultUriStatRecorderFactory(Provider<UriExtractorProviderLocator> uriExtractorProviderLocatorProvider, Provider<UriStatStorage> uriStatStorageProvider) {
        Objects.requireNonNull(uriExtractorProviderLocatorProvider, "uriExtractorProviderLocatorProvider");

        UriExtractorProviderLocator uriExtractorProviderLocator = uriExtractorProviderLocatorProvider.get();
        this.uriExtractorProviderLocator = Objects.requireNonNull(uriExtractorProviderLocator, "uriExtractorProviderLocator");

        Objects.requireNonNull(uriStatStorageProvider, "uriStatStorageProvider");
        this.uriStatStorage = uriStatStorageProvider.get();
    }

    @Override
    public <T> UriStatRecorder<T> create(UriExtractorService<T> uriExtractorService) {
        Objects.requireNonNull(uriExtractorService, "uriExtractorService");
        UriExtractor<T> uriExtractor = uriExtractorService.get(uriExtractorProviderLocator);

        if (uriExtractor == null) {
            return DisabledUriStatRecorder.create();
        } else {
            return new DefaultUriStatRecorder<T>(uriExtractor, uriStatStorage);
        }
    }

}
