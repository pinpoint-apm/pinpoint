/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.util.*;
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
                logger.warn("unsupported ParsingResult Type type {}", parsingResult);
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
