package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Function;

public class SqlCacheService<ID> {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private final CachingSqlNormalizer<ParsingResultInternal<ID>> cachingSqlNormalizer;

    private final EnhancedDataSender<MetaDataType, ResponseMessage> enhancedDataSender;

    public SqlCacheService(EnhancedDataSender<MetaDataType, ResponseMessage> enhancedDataSender, CachingSqlNormalizer<ParsingResultInternal<ID>> cachingSqlNormalizer) {
        this.enhancedDataSender = Objects.requireNonNull(enhancedDataSender, "enhancedDataSender");
        this.cachingSqlNormalizer = Objects.requireNonNull(cachingSqlNormalizer, "cachingSqlNormalizer");
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

            // isNewValue means that the value is newly cached.
            // So the sql could be new one. We have to send sql metadata to collector.
            final MetaDataType sqlMetaData = converter.apply(parsingResult);

            this.enhancedDataSender.request(sqlMetaData);
        } else {
            if (isDebug) {
                logger.debug("cache hit {}", parsingResult);
            }
        }
        return isNewValue;
    }


}
