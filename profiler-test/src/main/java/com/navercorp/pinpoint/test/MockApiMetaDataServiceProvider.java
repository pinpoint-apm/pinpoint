/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class MockApiMetaDataServiceProvider implements Provider<ApiMetaDataService> {

    private final Provider<EnhancedDataSender<MetaDataType>> enhancedDataSenderProvider;

    @Inject
    public MockApiMetaDataServiceProvider(Provider<EnhancedDataSender<MetaDataType>> enhancedDataSenderProvider) {
        this.enhancedDataSenderProvider = Objects.requireNonNull(enhancedDataSenderProvider, "enhancedDataSenderProvider");
    }

    @Override
    public ApiMetaDataService get() {
        final EnhancedDataSender<MetaDataType> enhancedDataSender = this.enhancedDataSenderProvider.get();
        return new MockApiMetaDataService(enhancedDataSender);
    }

}
