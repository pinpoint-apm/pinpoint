package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.profiler.cache.UidCache;

public class UidCachingSqlNormalizer extends AbstractCachingSqlNormalizer<byte[]> {
    public UidCachingSqlNormalizer(UidCache sqlCache) {
        super(sqlCache);
    }

    @Override
    protected ParsingResultInternal<byte[]> newParsingResult(String sql) {
        return new UidParsingResult(sql);
    }
}