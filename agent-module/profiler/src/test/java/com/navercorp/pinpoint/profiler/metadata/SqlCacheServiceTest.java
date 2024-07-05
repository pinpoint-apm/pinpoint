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
import com.navercorp.pinpoint.profiler.cache.IdAllocator;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SqlCacheServiceTest {

    @Test
    public void cacheSql() {
        final EnhancedDataSender<MetaDataType> dataSender = mock(EnhancedDataSender.class);
        SimpleCache<String> sqlCache = new SimpleCache<>(new IdAllocator.ZigZagAllocator(), 100);
        SimpleCachingSqlNormalizer simpleCachingSqlNormalizer = new SimpleCachingSqlNormalizer(sqlCache);
        final SqlCacheService<Integer> sqlMetaDataService = new SqlCacheService<>(dataSender, simpleCachingSqlNormalizer);

        final String sql = "select * from A";
        final ParsingResultInternal<Integer> parsingResult = new DefaultParsingResult(sql);

        boolean newValue = sqlMetaDataService.cacheSql(parsingResult, DefaultSqlMetaDataService::newSqlMetaData);

        Assertions.assertTrue(newValue);
        verify(dataSender).request(any(SqlMetaData.class));

        boolean notNewValue = sqlMetaDataService.cacheSql(parsingResult, DefaultSqlMetaDataService::newSqlMetaData);
        Assertions.assertFalse(notNewValue);
        verify(dataSender).request(any(SqlMetaData.class));
    }
}