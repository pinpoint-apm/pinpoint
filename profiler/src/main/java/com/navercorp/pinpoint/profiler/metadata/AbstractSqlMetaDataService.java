package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public abstract class AbstractSqlMetaDataService<ID> implements SqlMetaDataService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final CachingSqlNormalizer<ParsingResultInternal<ID>> cachingSqlNormalizer;

    private final EnhancedDataSender<MetaDataType, ResponseMessage> enhancedDataSender;

    protected AbstractSqlMetaDataService(EnhancedDataSender<MetaDataType, ResponseMessage> enhancedDataSender, CachingSqlNormalizer<ParsingResultInternal<ID>> cachingSqlNormalizer) {
        this.enhancedDataSender = Objects.requireNonNull(enhancedDataSender, "enhancedDataSender");
        this.cachingSqlNormalizer = Objects.requireNonNull(cachingSqlNormalizer, "cachingSqlNormalizer");
    }

    @Override
    public ParsingResult parseSql(String sql) {
        return this.cachingSqlNormalizer.wrapSql(sql);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean cacheSql(ParsingResult parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        // lazy sql parsing
        boolean isNewValue = this.cachingSqlNormalizer.normalizedSql((ParsingResultInternal<ID>) parsingResult);
        if (isNewValue) {
            if (isDebug) {
                // TODO logging hit ratio could help debugging
                logger.debug("NewSQLParsingResult:{}", parsingResult);
            }

            // isNewValue means that the value is newly cached.
            // So the sql could be new one. We have to send sql metadata to collector.
            final MetaDataType sqlMetaData = prepareSqlMetaData((ParsingResultInternal<ID>) parsingResult);

            this.enhancedDataSender.request(sqlMetaData);
        }
        return isNewValue;
    }

    protected abstract MetaDataType prepareSqlMetaData(ParsingResultInternal<ID> parsingResult);
}
