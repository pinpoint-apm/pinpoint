package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.message.DataConsumer;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.cache.Cache;
import com.navercorp.pinpoint.profiler.cache.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Function;

public class SqlCacheService<ID> {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private final CachingSqlNormalizer<ParsingResultInternal<ID>> cachingSqlNormalizer;

    private final DataConsumer<MetaDataType> dataSender;

    private final int trimSqlLength;

    public SqlCacheService(DataConsumer<MetaDataType> dataSender, Cache<String, Result<ID>> sqlCache, int trimSqlLength) {
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
        this.cachingSqlNormalizer = new DefaultCachingSqlNormalizer<>(sqlCache);
        this.trimSqlLength = trimSqlLength;
    }

    public boolean cacheSql(ParsingResultInternal<ID> parsingResult, Function<ParsingResultInternal<ID>, MetaDataType> converter) {
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

            // isNewValue means that the value is newly cached.
            // So the sql could be new one. We have to send sql metadata to collector.
            final MetaDataType sqlMetaData = converter.apply(parsingResult);

            this.dataSender.send(sqlMetaData);
        } else {
            if (isDebug) {
                logger.debug("cache hit {}", parsingResult);
            }
        }
        return isNewValue;
    }
}
