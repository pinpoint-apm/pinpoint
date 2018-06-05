/*
 * Copyright 2018 NAVER Corp.
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
import com.navercorp.pinpoint.common.util.DefaultJsonParser;
import com.navercorp.pinpoint.common.util.JsonParser;
import com.navercorp.pinpoint.common.util.NormalizedJson;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roy Kim
 */
public class DefaultJsonMetaDataService implements JsonMetaDataService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final DefaultParsingResult EMPTY_OBJECT = new DefaultParsingResult("");

    private final JsonParser jsonParser;

    private final String agentId;
    private final long agentStartTime;
    private final EnhancedDataSender enhancedDataSender;

    @Inject
    public DefaultJsonMetaDataService(ProfilerConfig profilerConfig, @AgentId String agentId,
                                      @AgentStartTime long agentStartTime, EnhancedDataSender enhancedDataSender) {
        this(agentId, agentStartTime, enhancedDataSender, profilerConfig.getJdbcSqlCacheSize());
    }

    public DefaultJsonMetaDataService(String agentId, long agentStartTime, EnhancedDataSender enhancedDataSender, int jdbcJsonCacheSize) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (enhancedDataSender == null) {
            throw new NullPointerException("enhancedDataSender must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.enhancedDataSender = enhancedDataSender;
        this.jsonParser = new DefaultJsonParser();
    }

    @Override
    public ParsingResult parseJson(final String json) {
        if (json == null) {
            return EMPTY_OBJECT;
        }

        ParsingResult parsingResult = new DefaultParsingResult(json);

        if (parsingResult == null) {
            return EMPTY_OBJECT;
        }

        if (!(parsingResult instanceof ParsingResultInternal)) {
            if (logger.isWarnEnabled()) {
                logger.warn("unsupported ParsingResult Type type {}", parsingResult);
            }
            throw new IllegalArgumentException("unsupported ParsingResult Type");
        }

        final ParsingResultInternal parsingResultInternal = (ParsingResultInternal) parsingResult;

        final String originalJson = parsingResultInternal.getOriginalSql();
        final NormalizedJson normalizedJson = this.jsonParser.normalizeJson(originalJson);

        parsingResultInternal.setSql(normalizedJson.getNormalizedJson());
        parsingResultInternal.setOutput(normalizedJson.getParseParameter());

        return parsingResult;
    }
}
