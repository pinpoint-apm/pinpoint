package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlNormalizer;
import com.navercorp.pinpoint.common.profiler.sql.NormalizedSql;
import com.navercorp.pinpoint.common.profiler.sql.SqlNormalizer;
import com.navercorp.pinpoint.profiler.cache.Cache;
import com.navercorp.pinpoint.profiler.cache.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultCachingSqlNormalizer<ID> implements CachingSqlNormalizer<ParsingResultInternal<ID>> {
    private static final Logger logger = LogManager.getLogger(DefaultCachingSqlNormalizer.class);

    private final Cache<String, Result<ID>> sqlCache;
    private final SqlNormalizer sqlNormalizer;

    public DefaultCachingSqlNormalizer(Cache<String, Result<ID>> sqlCache, boolean removeComments) {
        this.sqlCache = sqlCache;
        this.sqlNormalizer = new DefaultSqlNormalizer(removeComments);
    }

    @Override
    public boolean normalizedSql(ParsingResultInternal<ID> parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        if (parsingResult.getId() != null) {
            // already cached
            return false;
        }

        final String originalSql = parsingResult.getOriginalSql();

        final NormalizedSql normalizedSql = this.sqlNormalizer.normalizeSql(originalSql);
        final Result<ID> cachingResult = this.sqlCache.put(normalizedSql.getNormalizedSql());

        setParsingResult(parsingResult, cachingResult.getId(), normalizedSql);

        return cachingResult.isNewValue();
    }

    private void setParsingResult(ParsingResultInternal<ID> parsingResult, ID id, NormalizedSql normalizedSql) {
        boolean success = parsingResult.setId(id);
        if (!success) {
            if (logger.isWarnEnabled()) {
                logger.warn("invalid state. setSqlId fail setId:{}, ParsingResultInternal:{}", id, parsingResult);
            }
        }
        parsingResult.setSql(normalizedSql.getNormalizedSql());
        parsingResult.setOutput(normalizedSql.getParseParameter());
    }
}
