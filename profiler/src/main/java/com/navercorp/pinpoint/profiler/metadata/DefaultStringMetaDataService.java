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

package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultStringMetaDataService implements StringMetaDataService {

    private final SimpleCache<String> stringCache;

    private final EnhancedDataSender<Object> enhancedDataSender;

    public DefaultStringMetaDataService(EnhancedDataSender<Object> enhancedDataSender, SimpleCache<String> stringCache) {
        this.enhancedDataSender = Assert.requireNonNull(enhancedDataSender, "enhancedDataSender");
        this.stringCache = Assert.requireNonNull(stringCache, "stringCache");

    }

    @Override
    public int cacheString(final String value) {
        if (value == null) {
            return 0;
        }
        final Result result = this.stringCache.put(value);
        if (result.isNewValue()) {
            final StringMetaData stringMetaData = new StringMetaData(result.getId(), value);
            this.enhancedDataSender.request(stringMetaData);
        }
        return result.getId();
    }
}
