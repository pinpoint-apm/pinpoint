/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.logback;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.List;

/**
 * @author Minwoo Jung
 *
 */
public class LogbackConfig {

    private static final String LOGBACK_LOGGING_TRANSACTION_INFO = "profiler.logback.logging.transactioninfo";

    private static final String LOGBACK_LOGGING_PATTERN_REPLACE_ENABLE = "profiler.logback.logging.pattern.replace.enable";
    private static final String LOGBACK_LOGGING_PATTERN_REPLACE_SEARCH = "profiler.logback.logging.pattern.replace.search";
    private static final String LOGBACK_LOGGING_PATTERN_REPLACE_WITH = "profiler.logback.logging.pattern.replace.with";

    private final boolean logbackLoggingTransactionInfo;

    private final boolean patternReplaceEnable;
    private final List<String> patternReplaceSearchList;
    private final String patternReplaceWith;


    public LogbackConfig(ProfilerConfig config) {
        this.logbackLoggingTransactionInfo = config.readBoolean(LOGBACK_LOGGING_TRANSACTION_INFO, false);

        this.patternReplaceSearchList = config.readList(LOGBACK_LOGGING_PATTERN_REPLACE_SEARCH);
        this.patternReplaceWith = config.readString(LOGBACK_LOGGING_PATTERN_REPLACE_WITH, "");
        boolean configEnabled = config.readBoolean(LOGBACK_LOGGING_PATTERN_REPLACE_ENABLE, false);
        boolean configOk = !CollectionUtils.isEmpty(patternReplaceSearchList) && StringUtils.hasText(patternReplaceWith);
        this.patternReplaceEnable = configEnabled && configOk;
    }
    
    public boolean isLogbackLoggingTransactionInfo() {
        return logbackLoggingTransactionInfo;
    }

    public boolean isPatternReplaceEnable() {
        return patternReplaceEnable;
    }

    public List<String> getPatternReplaceSearchList() {
        return patternReplaceSearchList;
    }

    public String getPatternReplaceWith() {
        return patternReplaceWith;
    }

    @Override
    public String toString() {
        return "LogbackConfig{" +
                "logbackLoggingTransactionInfo=" + logbackLoggingTransactionInfo +
                ", patternReplaceEnable=" + patternReplaceEnable +
                ", patternReplaceSearchList=" + patternReplaceSearchList +
                ", patternReplaceWith='" + patternReplaceWith + '\'' +
                '}';
    }
}
