package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.profiler.cache.SimpleCache;

public class SimpleCachingSqlNormalizer extends AbstractCachingSqlNormalizer<Integer> {
    public SimpleCachingSqlNormalizer(SimpleCache<String> sqlCache) {
        super(sqlCache);
    }

    @Override
    protected ParsingResultInternal<Integer> newParsingResult(String sql) {
        return new DefaultParsingResult(sql);
    }
}
