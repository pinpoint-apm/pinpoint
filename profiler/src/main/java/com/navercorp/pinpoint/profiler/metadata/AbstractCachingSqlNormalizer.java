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

import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlParser;
import com.navercorp.pinpoint.common.profiler.sql.NormalizedSql;
import com.navercorp.pinpoint.common.profiler.sql.SqlParser;
import com.navercorp.pinpoint.profiler.cache.Cache;
import com.navercorp.pinpoint.profiler.cache.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author emeroad
 */
public abstract class AbstractCachingSqlNormalizer<ID> implements CachingSqlNormalizer<ParsingResultInternal<ID>> {
    protected final ParsingResultInternal<ID> EMPTY_OBJECT = newParsingResult("");

    protected final Logger logger = LogManager.getLogger(this.getClass());

    private final Cache<String, Result<ID>> sqlCache;
    private final SqlParser sqlParser;

    public AbstractCachingSqlNormalizer(Cache<String, Result<ID>> sqlCache) {
        this.sqlCache = Objects.requireNonNull(sqlCache, "sqlCache");
        this.sqlParser = new DefaultSqlParser();
    }

    @Override
    public ParsingResultInternal<ID> wrapSql(String sql) {
        if (sql == null) {
            return EMPTY_OBJECT;
        }
        return newParsingResult(sql);
    }

    @Override
    public boolean normalizedSql(ParsingResultInternal<ID> parsingResult) {
        if (parsingResult == null) {
            return false;
        }
        if (parsingResult == EMPTY_OBJECT) {
            return false;
        }
        if (parsingResult.getId() != null) {
            // already cached
            return false;
        }

        final String originalSql = parsingResult.getOriginalSql();
        final NormalizedSql normalizedSql = this.sqlParser.normalizedSql(originalSql);

        final Result<ID> cachingResult = this.sqlCache.put(normalizedSql.getNormalizedSql());

        boolean success = parsingResult.setId(cachingResult.getId());
        if (!success) {
            if (logger.isWarnEnabled()) {
                logger.warn("invalid state. setSqlId fail setId:{}, ParsingResultInternal:{}", cachingResult.getId(), parsingResult);
            }
        }
        parsingResult.setSql(normalizedSql.getNormalizedSql());
        parsingResult.setOutput(normalizedSql.getParseParameter());

        return cachingResult.isNewValue();
    }

    protected abstract ParsingResultInternal<ID> newParsingResult(String sql);
}
