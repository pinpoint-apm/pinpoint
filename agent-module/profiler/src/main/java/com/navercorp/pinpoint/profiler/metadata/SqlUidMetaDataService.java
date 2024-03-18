package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.BytesStringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;

import java.util.Objects;

public class SqlUidMetaDataService implements SqlMetaDataService {

    private static final UidParsingResult EMPTY_RESULT = new UidParsingResult("");

    private final SqlCacheService<byte[]> sqlCacheService;

    public SqlUidMetaDataService(SqlCacheService<byte[]> sqlCacheService) {
        this.sqlCacheService = Objects.requireNonNull(sqlCacheService, "sqlCacheService");
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
            this.sqlCacheService.cacheSql(result, SqlUidMetaDataService::newSqlUidMetaData);
        }

        String output = StringUtils.defaultIfEmpty(parsingResult.getOutput(), null);
        bindValue = StringUtils.defaultIfEmpty(bindValue, null);

        final BytesStringStringValue sqlValue = new BytesStringStringValue(result.getId(), output, bindValue);
        return Annotations.of(AnnotationKey.SQL_UID.getCode(), sqlValue);
    }

    static MetaDataType newSqlUidMetaData(ParsingResultInternal<byte[]> parsingResult) {
        return new SqlUidMetaData(parsingResult.getId(), parsingResult.getSql());
    }
}
