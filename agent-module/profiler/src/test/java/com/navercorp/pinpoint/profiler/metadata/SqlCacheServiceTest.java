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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @author Woonduk Kang(emeroad)
 */
@ExtendWith(MockitoExtension.class)
public class SqlCacheServiceTest {
    private static final int MAX_LENGTH = 1000;

    private SqlCacheService<Integer> sut;

    @Mock
    private EnhancedDataSender<MetaDataType> dataSender;

    @BeforeEach
    public void setUp() {
        SimpleCache<String> sqlCache = new SimpleCache<>(new IdAllocator.ZigZagAllocator(), 100);
        SimpleCachingSqlNormalizer cachingSqlNormalizer = new SimpleCachingSqlNormalizer(sqlCache);
        sut = new SqlCacheService<>(dataSender, cachingSqlNormalizer, MAX_LENGTH);
    }

    @Test
    public void cacheSql() {
        final String sql = "select * from A";
        final ParsingResultInternal<Integer> parsingResult = new DefaultParsingResult(sql);

        boolean newValue = sut.cacheSql(parsingResult, DefaultSqlMetaDataService::newSqlMetaData);
        boolean notNewValue = sut.cacheSql(parsingResult, DefaultSqlMetaDataService::newSqlMetaData);

        assertTrue(newValue);
        verify(dataSender).request(any(SqlMetaData.class));

        Assertions.assertFalse(notNewValue);
        verifyNoMoreInteractions(dataSender);
    }

    @Test
    public void trimSql() {
        final String sql = veryLongString();
        final ParsingResultInternal<Integer> parsingResult = new DefaultParsingResult(sql);

        sut.cacheSql(parsingResult, DefaultSqlMetaDataService::newSqlMetaData);

        assertTrue(parsingResult.getSql().length() < sql.length());
    }

    private String veryLongString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MAX_LENGTH + 100; i++) {
            builder.append("a");
        }
        return builder.toString();
    }
}