package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.common.util.*;
import com.navercorp.pinpoint.profiler.metadata.Result;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DefaultCachingSqlNormalizer implements CachingSqlNormalizer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final DefaultParsingResult EMPTY_OBJECT = new DefaultParsingResult("");

    private final SimpleCache<String> sqlCache;
    private final SqlParser sqlParser;

    public DefaultCachingSqlNormalizer(int cacheSize) {
        this.sqlCache = new SimpleCache<String>(cacheSize);
        this.sqlParser = new DefaultSqlParser();
    }

    @Override
    public ParsingResult wrapSql(String sql) {
        if (sql == null) {
            return EMPTY_OBJECT;
        }
        return new DefaultParsingResult(sql);
    }

    @Override
    public boolean normalizedSql(ParsingResult parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        if (parsingResult == EMPTY_OBJECT) {
            return false;
        }
        if (parsingResult.getId() != ParsingResult.ID_NOT_EXIST) {
            // already cached
            return false;
        }

        if (!(parsingResult instanceof ParsingResultInternal)) {
            if (logger.isWarnEnabled()) {
                logger.warn("unsupported ParsingResult Type type {}");
            }
            throw new IllegalArgumentException("unsupported ParsingResult Type");
        }

        final ParsingResultInternal parsingResultInternal = (ParsingResultInternal) parsingResult;

        final String originalSql = parsingResultInternal.getOriginalSql();
        final NormalizedSql normalizedSql = this.sqlParser.normalizedSql(originalSql);

        final Result cachingResult = this.sqlCache.put(normalizedSql.getNormalizedSql());

        // set normalizedSql
        // set sqlId
        final boolean success = parsingResultInternal.setId(cachingResult.getId());
        if (!success) {
            if (logger.isWarnEnabled()) {
                logger.warn("invalid state. setSqlId fail setId:{}, ParsingResultInternal:{}", cachingResult.getId(), parsingResultInternal);
            }
        }

        parsingResultInternal.setSql(normalizedSql.getNormalizedSql());
        parsingResultInternal.setOutput(normalizedSql.getParseParameter());

        return cachingResult.isNewValue();
    }


}

