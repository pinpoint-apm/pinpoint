/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.metadata;

import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSqlMetaDataService implements SqlMetaDataService {

    private static final DefaultParsingResult EMPTY_RESULT = new DefaultParsingResult("");

    private final SqlCacheService<Integer> sqlCacheService;

    public DefaultSqlMetaDataService(SqlCacheService<Integer> sqlCacheService) {
        this.sqlCacheService = Objects.requireNonNull(sqlCacheService, "sqlCacheService");
    }

    @Override
    public ParsingResult wrapSqlResult(String sql) {
        if (sql == null) {
            return EMPTY_RESULT;
        }
        return new DefaultParsingResult(sql);
    }

    @Override
    public Annotation<?> newSqlAnnotation(ParsingResult parsingResult, String bindValue) {
        if (!(parsingResult instanceof DefaultParsingResult)) {
            throw new IllegalStateException("Unexpected DefaultParsingResult :" + parsingResult);
        }


        final ParsingResultInternal<Integer> result = (DefaultParsingResult) parsingResult;
        if (result != EMPTY_RESULT) {
            this.sqlCacheService.cacheSql(result, DefaultSqlMetaDataService::newSqlMetaData);
        }

        String output = StringUtils.defaultIfEmpty(parsingResult.getOutput(), null);
        bindValue = StringUtils.defaultIfEmpty(bindValue, null);

        final IntStringStringValue sqlValue = new IntStringStringValue(result.getId(), output, bindValue);
        return Annotations.of(AnnotationKey.SQL_ID.getCode(), sqlValue);
    }

    static MetaDataType newSqlMetaData(ParsingResultInternal<Integer> parsingResult) {
        return new SqlMetaData(parsingResult.getId(), parsingResult.getSql());
    }
}
