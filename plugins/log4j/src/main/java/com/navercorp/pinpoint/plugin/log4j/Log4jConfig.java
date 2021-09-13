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
package com.navercorp.pinpoint.plugin.log4j;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.List;

/**
 * @author Minwoo Jung
 */
public class Log4jConfig {
    public static final String LOG4J_LOGGING_TRANSACTION_INFO = "profiler.log4j.logging.transactioninfo";

    private final boolean log4jLoggingTransactionInfo;

    private static final String LOGGING_PATTERN_REPLACE_ENABLE = "profiler.log4j.logging.pattern.replace.enable";
    private static final String LOGGING_PATTERN_REPLACE_SEARCH = "profiler.log4j.logging.pattern.replace.search";
    private static final String LOGGING_PATTERN_REPLACE_WITH = "profiler.log4j.logging.pattern.replace.with";

    private final boolean patternReplaceEnable;
    private final List<String> patternReplaceSearchList;
    private final String patternReplaceWith;


    public Log4jConfig(ProfilerConfig config) {
        this.log4jLoggingTransactionInfo = config.readBoolean(LOG4J_LOGGING_TRANSACTION_INFO, false);

        this.patternReplaceSearchList = config.readList(LOGGING_PATTERN_REPLACE_SEARCH);
        this.patternReplaceWith = config.readString(LOGGING_PATTERN_REPLACE_WITH, "");
        boolean configEnabled = config.readBoolean(LOGGING_PATTERN_REPLACE_ENABLE, false);
        boolean configOk = !CollectionUtils.isEmpty(patternReplaceSearchList) && StringUtils.hasText(patternReplaceWith);
        this.patternReplaceEnable = configEnabled && configOk;
    }

    public boolean isLog4jLoggingTransactionInfo() {
        return log4jLoggingTransactionInfo;
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
        return "Log4jConfig{" +
                "log4jLoggingTransactionInfo=" + log4jLoggingTransactionInfo +
                ", patternReplaceEnable=" + patternReplaceEnable +
                ", patternReplaceSearchList=" + patternReplaceSearchList +
                ", patternReplaceWith='" + patternReplaceWith + '\'' +
                '}';
    }

}
