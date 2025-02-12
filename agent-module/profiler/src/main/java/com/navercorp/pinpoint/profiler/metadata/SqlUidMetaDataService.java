package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.common.profiler.message.DataConsumer;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.BytesStringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;

import java.util.Objects;

public class SqlUidMetaDataService implements SqlMetaDataService {

    private static final UidParsingResult EMPTY_RESULT = new UidParsingResult("");

    private final SqlCacheService<byte[]> sqlCacheService;

    private final DataConsumer<MetaDataType> dataSender;

    public SqlUidMetaDataService(DataConsumer<MetaDataType> dataSender, SqlCacheService<byte[]> sqlCacheService) {
        this.sqlCacheService = Objects.requireNonNull(sqlCacheService, "sqlCacheService");
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
    }

    @Override
    public ParsingResult wrapSqlResult(String sql) {
        if (sql == null) {
            return EMPTY_RESULT;
        }
        return new UidParsingResult(sql);
    }

    @Override
    public Annotation<?> newSqlAnnotation(ParsingResult parsingResult, String bindValue) {
        if (!(parsingResult instanceof UidParsingResult)) {
            throw new IllegalStateException("Unexpected UidParsingResult :" + parsingResult);
        }

        final ParsingResultInternal<byte[]> result = (UidParsingResult) parsingResult;
        if (result != EMPTY_RESULT) {
            boolean isNewValue = this.sqlCacheService.cacheSql(result);

            if (isNewValue) {
                final MetaDataType sqlMetaData = new SqlUidMetaData(result.getId(), result.getSql());
                dataSender.send(sqlMetaData);
            }
        }

        String output = StringUtils.defaultIfEmpty(parsingResult.getOutput(), null);
        bindValue = StringUtils.defaultIfEmpty(bindValue, null);

        final BytesStringStringValue sqlValue = new BytesStringStringValue(result.getId(), output, bindValue);
        return Annotations.of(AnnotationKey.SQL_UID.getCode(), sqlValue);
    }
}
