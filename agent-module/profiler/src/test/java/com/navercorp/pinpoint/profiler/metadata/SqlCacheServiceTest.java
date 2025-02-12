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

import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SqlCacheServiceTest {
    static final int MAX_LENGTH = 1000;

    SqlCacheService<Integer> sut;

    static String veryLongString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < MAX_LENGTH + 100; i++) {
            builder.append("a");
        }
        return builder.toString();
    }

    @BeforeEach
    void setUp() {
        SimpleCache<String, Integer> sqlCache = SimpleCache.newIdCache(100);
        sut = new SqlCacheService<>(sqlCache, MAX_LENGTH);
    }

    @Test
    void cacheSql() {
        final String sql = "select * from A";
        final ParsingResultInternal<Integer> parsingResult = new DefaultParsingResult(sql);

        boolean newValue = sut.cacheSql(parsingResult);
        boolean notNewValue = sut.cacheSql(parsingResult);

        assertTrue(newValue);
        assertFalse(notNewValue);
    }

    @Test
    void trimSql() {
        final String sql = veryLongString();
        final ParsingResultInternal<Integer> parsingResult = new DefaultParsingResult(sql);

        sut.cacheSql(parsingResult);

        assertTrue(parsingResult.getSql().length() < sql.length());
    }
}
