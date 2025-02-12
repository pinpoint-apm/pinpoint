package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.cache.Cache;
import com.navercorp.pinpoint.profiler.cache.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SqlCacheService<ID> {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private final CachingSqlNormalizer<ParsingResultInternal<ID>> cachingSqlNormalizer;

    private final int trimSqlLength;

    public SqlCacheService(Cache<String, Result<ID>> sqlCache, int trimSqlLength) {
        this.cachingSqlNormalizer = new DefaultCachingSqlNormalizer<>(sqlCache);
        this.trimSqlLength = trimSqlLength;
    }

    public boolean cacheSql(ParsingResultInternal<ID> parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        // lazy sql parsing
        boolean isNewValue = this.cachingSqlNormalizer.normalizedSql(parsingResult);
        if (isNewValue) {
            if (isDebug) {
                // TODO logging hit ratio could help debugging
                logger.debug("update sql cache {}", parsingResult);
            }

            // trim long sql
            if (parsingResult.getSql().length() > trimSqlLength) {
                String trimmedSql = StringUtils.abbreviate(parsingResult.getSql(), trimSqlLength);
                parsingResult.setSql(trimmedSql);
            }
        } else {
            if (isDebug) {
                logger.debug("cache hit {}", parsingResult);
            }
        }
        return isNewValue;
    }
}
