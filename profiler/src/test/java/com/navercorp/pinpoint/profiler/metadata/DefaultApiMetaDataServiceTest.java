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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.profiler.context.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultApiMetaDataServiceTest {

    @Test
    public void cacheApi() throws Exception {
        EnhancedDataSender<Object> dataSender = mock(EnhancedDataSender.class);
        SimpleCache<String> cache = new SimpleCache<String>(new SimpleCache.ZigZagTransformer());
        ApiMetaDataService apiMetaDataService = new DefaultApiMetaDataService(dataSender, cache);

        MethodDescriptor methodDescriptor = new DefaultMethodDescriptor("clazz", "method", null, null);

        int first = apiMetaDataService.cacheApi(methodDescriptor);

        Assert.assertNotEquals("not exist", first, 0);
        verify(dataSender, times(1)).request(any(ApiMetaData.class));

        int second = apiMetaDataService.cacheApi(methodDescriptor);
        Assert.assertEquals("check cache", first, second);
        verify(dataSender, times(1)).request(any(ApiMetaData.class));
    }

}