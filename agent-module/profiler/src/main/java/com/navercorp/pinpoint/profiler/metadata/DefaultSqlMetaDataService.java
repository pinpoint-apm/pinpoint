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
import com.navercorp.pinpoint.common.profiler.message.DataConsumer;
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

    private final DataConsumer<MetaDataType> dataSender;

    public DefaultSqlMetaDataService(DataConsumer<MetaDataType> dataSender, SqlCacheService<Integer> sqlCacheService) {
        this.sqlCacheService = Objects.requireNonNull(sqlCacheService, "sqlCacheService");
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
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
            boolean isNewValue = this.sqlCacheService.cacheSql(result);

            if (isNewValue) {
                final MetaDataType sqlMetaData = new SqlMetaData(result.getId(), result.getSql());
                dataSender.send(sqlMetaData);
            }
        }

        String output = StringUtils.defaultIfEmpty(parsingResult.getOutput(), null);
        bindValue = StringUtils.defaultIfEmpty(bindValue, null);

        final IntStringStringValue sqlValue = new IntStringStringValue(result.getId(), output, bindValue);
        return Annotations.of(AnnotationKey.SQL_ID.getCode(), sqlValue);
    }
}
