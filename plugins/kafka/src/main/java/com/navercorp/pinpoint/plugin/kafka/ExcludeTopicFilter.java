/*
 * Copyright 2022 NAVER Corp.
 *
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

package com.navercorp.pinpoint.plugin.kafka;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.StringUtils;

public class ExcludeTopicFilter implements Filter<String> {

    static final String PATH_SEPARATOR = ",";

    @VisibleForTesting
    static final int MAX_LENGTH = 249;

    @VisibleForTesting
    static final String TOPIC_PATTERN_VALUE = "[a-zA-Z0-9._-]+";

    private static final Pattern TOPIC_PATTERN = Pattern.compile(TOPIC_PATTERN_VALUE);

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final Set<String> excludeTopicSet;

    public ExcludeTopicFilter(String excludeFormat) {
        final List<String> splitList = StringUtils.tokenizeToStringList(excludeFormat, PATH_SEPARATOR);
        this.excludeTopicSet = new HashSet<>();
        for (String topic : splitList) {
            if (!checkLength(topic)) {
                logger.info("Topic({}) size must be 1 ~ {}.", topic, MAX_LENGTH);
                continue;
            }
            if (!checkPattern(topic)) {
                logger.info("Topic({}) pattern must be {}.", topic, TOPIC_PATTERN_VALUE);
                continue;
            }
            excludeTopicSet.add(topic);
        }
    }

    private boolean checkLength(String topic) {
        final int topicLength = StringUtils.getLength(topic);
        if (topicLength == 0) {
            return false;
        }
        return topicLength <= MAX_LENGTH;
    }

    private boolean checkPattern(String topic) {
        final Matcher matcher = TOPIC_PATTERN.matcher(topic);
        return matcher.matches();
    }

    @Override
    public boolean filter(String topic) {
        return excludeTopicSet.contains(topic);
    }
}
