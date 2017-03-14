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

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ParsingResult;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.thrift.dto.TSqlMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultSqlMetaDataService implements SqlMetaDataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final CachingSqlNormalizer cachingSqlNormalizer;

    private final String agentId;
    private final long agentStartTime;
    private final EnhancedDataSender enhancedDataSender;

    @Inject
    public DefaultSqlMetaDataService(ProfilerConfig profilerConfig, @AgentId String agentId,
                                     @AgentStartTime long agentStartTime, EnhancedDataSender enhancedDataSender) {
        this(agentId, agentStartTime, enhancedDataSender, profilerConfig.getJdbcSqlCacheSize());
    }

    public DefaultSqlMetaDataService(String agentId, long agentStartTime, EnhancedDataSender enhancedDataSender, int jdbcSqlCacheSize) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (enhancedDataSender == null) {
            throw new NullPointerException("enhancedDataSender must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.enhancedDataSender = enhancedDataSender;
        this.cachingSqlNormalizer = new DefaultCachingSqlNormalizer(jdbcSqlCacheSize);
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
            final TSqlMetaData sqlMetaData = new TSqlMetaData();
            sqlMetaData.setAgentId(agentId);
            sqlMetaData.setAgentStartTime(agentStartTime);

            sqlMetaData.setSqlId(parsingResult.getId());
            sqlMetaData.setSql(parsingResult.getSql());

            this.enhancedDataSender.request(sqlMetaData);
        }
        return isNewValue;
    }

}
