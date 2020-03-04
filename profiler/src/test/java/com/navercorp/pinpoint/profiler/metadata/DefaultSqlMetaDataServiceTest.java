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

import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSqlMetaDataServiceTest {

    @Test
    public void cacheSql() throws Exception {
        final EnhancedDataSender dataSender = mock(EnhancedDataSender.class);
        SimpleCache<String> sqlCache = new SimpleCache<String>(new SimpleCache.ZigZagTransformer(), 100);
        final SqlMetaDataService sqlMetaDataService = new DefaultSqlMetaDataService(dataSender, sqlCache);

        final String sql = "select * from A";
        final ParsingResult parsingResult = sqlMetaDataService.parseSql(sql);

        boolean newValue = sqlMetaDataService.cacheSql(parsingResult);

        Assert.assertTrue(newValue);
        verify(dataSender, times(1)).request(any(SqlMetaData.class));

        boolean notNewValue = sqlMetaDataService.cacheSql(parsingResult);
        Assert.assertFalse(notNewValue);
        verify(dataSender, times(1)).request(any(SqlMetaData.class));
    }
}