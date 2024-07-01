package com.navercorp.pinpoint.profiler.metadata;

import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlNormalizer;
import com.navercorp.pinpoint.common.profiler.sql.NormalizedSql;
import com.navercorp.pinpoint.common.profiler.sql.SqlNormalizer;
import com.navercorp.pinpoint.profiler.cache.Cache;
import com.navercorp.pinpoint.profiler.cache.Result;
import com.navercorp.pinpoint.profiler.cache.UidCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class UidCachingSqlNormalizer implements CachingSqlNormalizer<ParsingResultInternal<byte[]>> {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Function<String, byte[]> hashFunction = x -> Hashing.murmur3_128().hashString(x, StandardCharsets.UTF_8).asBytes();

    private final Cache<String, Result<byte[]>> sqlCache;
    private final SqlNormalizer sqlNormalizer;
    private final int lengthLimit;

    public UidCachingSqlNormalizer(int cacheSize, int lengthLimit) {
        this.sqlCache = new UidCache(cacheSize, hashFunction);
        this.sqlNormalizer = new DefaultSqlNormalizer();
        this.lengthLimit = lengthLimit;
    }

    @Override
    public boolean normalizedSql(ParsingResultInternal<byte[]> parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        if (parsingResult.getId() != null) {
            // already cached
            return false;
        }

        final String originalSql = parsingResult.getOriginalSql();
        final NormalizedSql normalizedSql = this.sqlNormalizer.normalizeSql(originalSql);

        byte[] uid;
        boolean isNewValue;
        if (lengthLimit == -1 || normalizedSql.getNormalizedSql().length() <= lengthLimit) {
            final Result<byte[]> cachingResult = this.sqlCache.put(normalizedSql.getNormalizedSql());
            uid = cachingResult.getId();
            isNewValue = cachingResult.isNewValue();
        } else {
            // bypass cache
            uid = hashFunction.apply(normalizedSql.getNormalizedSql());
            isNewValue = true;
        }

        setParsingResult(parsingResult, uid, normalizedSql);
        return isNewValue;
    }

    private void setParsingResult(ParsingResultInternal<byte[]> parsingResult, byte[] uid, NormalizedSql normalizedSql) {
        boolean success = parsingResult.setId(uid);
        if (!success) {
            if (logger.isWarnEnabled()) {
                logger.warn("invalid state. setSqlUid fail setUid:{}, ParsingResultInternal:{}", uid, parsingResult);
            }
        }
        parsingResult.setSql(normalizedSql.getNormalizedSql());
        parsingResult.setOutput(normalizedSql.getParseParameter());
    }
}
