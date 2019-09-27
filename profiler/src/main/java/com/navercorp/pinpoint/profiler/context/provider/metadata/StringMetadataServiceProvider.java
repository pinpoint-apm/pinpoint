/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.metadata;

import com.google.inject.Inject;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.metadata.DefaultStringMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

import javax.inject.Provider;


/**
 * @author Woonduk Kang(emeroad)
 */
public class StringMetadataServiceProvider implements Provider<StringMetaDataService> {

    private final EnhancedDataSender<Object> enhancedDataSender;
    private final SimpleCacheFactory simpleCacheFactory;

    @Inject
    public StringMetadataServiceProvider(@MetadataDataSender EnhancedDataSender<Object> enhancedDataSender, SimpleCacheFactory simpleCacheFactory) {
        this.enhancedDataSender = Assert.requireNonNull(enhancedDataSender, "enhancedDataSender");
        this.simpleCacheFactory = Assert.requireNonNull(simpleCacheFactory, "simpleCacheFactory");
    }

    @Override
    public StringMetaDataService get() {
        final SimpleCache<String> stringCache = simpleCacheFactory.newSimpleCache();
        return new DefaultStringMetaDataService(enhancedDataSender, stringCache);
    }
}
