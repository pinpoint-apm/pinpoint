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
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSqlMetaDataService implements SqlMetaDataService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final CachingSqlNormalizer cachingSqlNormalizer;

    private final EnhancedDataSender<MetaDataType, ResponseMessage> enhancedDataSender;

    public DefaultSqlMetaDataService(EnhancedDataSender<MetaDataType, ResponseMessage> enhancedDataSender, SimpleCache<String> sqlCache) {
        this.enhancedDataSender = Objects.requireNonNull(enhancedDataSender, "enhancedDataSender");

        Objects.requireNonNull(sqlCache, "sqlCache");
        this.cachingSqlNormalizer = new DefaultCachingSqlNormalizer(sqlCache);
    }

    @Override
    public ParsingResult parseSql(final String sql) {
        // lazy sql normalization
        return this.cachingSqlNormalizer.wrapSql(sql);
    }


    @Override
    public boolean cacheSql(ParsingResult parsingResult) {

        if (parsingResult == null) {
            return false;
        }
        // lazy sql parsing
        boolean isNewValue = this.cachingSqlNormalizer.normalizedSql(parsingResult);
        if (isNewValue) {
            if (isDebug) {
                // TODO logging hit ratio could help debugging
                logger.debug("NewSQLParsingResult:{}", parsingResult);
            }

            // isNewValue means that the value is newly cached.
            // So the sql could be new one. We have to send sql metadata to collector.
            final SqlMetaData sqlMetaData = new SqlMetaData(parsingResult.getId(), parsingResult.getSql());

            this.enhancedDataSender.request(sqlMetaData);
        }
        return isNewValue;
    }

}
