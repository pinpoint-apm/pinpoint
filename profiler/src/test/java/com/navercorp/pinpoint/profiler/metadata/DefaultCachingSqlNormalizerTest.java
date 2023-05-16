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
import com.navercorp.pinpoint.profiler.cache.IdAllocator;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class DefaultCachingSqlNormalizerTest {

    @Test
    public void testNormalizedSql() {
        SimpleCache<String> cache = newCache(1);
        CachingSqlNormalizer normalizer = new DefaultCachingSqlNormalizer(cache);
        ParsingResult parsingResult = normalizer.wrapSql("select * from dual");

        boolean newCache = normalizer.normalizedSql(parsingResult);
        Assertions.assertTrue(newCache, "newCacheState");

        boolean notCached = normalizer.normalizedSql(parsingResult);
        Assertions.assertFalse(notCached, "alreadyCached");

        ParsingResult alreadyCached = normalizer.wrapSql("select * from dual");
        boolean notCached2 = normalizer.normalizedSql(alreadyCached);
        Assertions.assertFalse(notCached2, "alreadyCached2");
    }


    @Test
    public void testNormalizedSql_cache_expire() {
        SimpleCache<String> cache = newCache(1);
        CachingSqlNormalizer normalizer = new DefaultCachingSqlNormalizer(cache);
        ParsingResult parsingResult = normalizer.wrapSql("select * from table1");
        boolean newCache = normalizer.normalizedSql(parsingResult);
        Assertions.assertTrue(newCache, "newCacheState");

        // cache expire
        ParsingResult parsingResult2 = normalizer.wrapSql("select * from table2");
        boolean cached = normalizer.normalizedSql(parsingResult2);
        Assertions.assertTrue(cached);

        ParsingResult parsingResult1_recached = normalizer.wrapSql("select * from table3");
        boolean newCache_parsingResult1_recached = normalizer.normalizedSql(parsingResult1_recached);
        Assertions.assertTrue(newCache_parsingResult1_recached);
    }

    private SimpleCache<String> newCache(int size) {
        return new SimpleCache<>(new IdAllocator.ZigZagAllocator(), size);
    }
}