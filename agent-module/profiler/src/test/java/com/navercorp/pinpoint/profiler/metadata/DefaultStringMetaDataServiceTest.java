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

import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.cache.IdAllocator;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultStringMetaDataServiceTest {

    @Test
    public void cacheString() {
        EnhancedDataSender<MetaDataType, ResponseMessage> dataSender = mock(EnhancedDataSender.class);
        SimpleCache<String> stringCache = new SimpleCache<>(new IdAllocator.ZigZagAllocator());
        StringMetaDataService stringMetaDataService = new DefaultStringMetaDataService(dataSender, stringCache);

        String str = "test";

        int first = stringMetaDataService.cacheString(str);

        Assertions.assertNotEquals(first, 0, "not exist");
        verify(dataSender).request(any(StringMetaData.class));

        int second = stringMetaDataService.cacheString(str);
        Assertions.assertEquals(first, second, "check cache");
        verify(dataSender).request(any(StringMetaData.class));
    }

}