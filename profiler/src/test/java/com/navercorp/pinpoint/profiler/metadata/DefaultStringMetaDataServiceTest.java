/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultStringMetaDataServiceTest {

    @Test
    public void cacheString() throws Exception {
        EnhancedDataSender dataSender = mock(EnhancedDataSender.class);
        StringMetaDataService stringMetaDataService = new DefaultStringMetaDataService("agentId", System.currentTimeMillis(), dataSender);

        String str = "test";

        int first = stringMetaDataService.cacheString(str);

        Assert.assertNotEquals("not exist", first, 0);
        verify(dataSender, times(1)).request(any(TBase.class));

        int second = stringMetaDataService.cacheString(str);
        Assert.assertEquals("check cache", first, second);
        verify(dataSender, times(1)).request(any(TBase.class));
    }

}