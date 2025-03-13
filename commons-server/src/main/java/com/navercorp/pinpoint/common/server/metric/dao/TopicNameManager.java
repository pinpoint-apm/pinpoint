/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.metric.dao;

/**
 * @author minwoo-jung
 */
public class TopicNameManager extends TableNameManager {

    public TopicNameManager(String topicPrefix, int paddingLength, int count) {
        super(topicPrefix, paddingLength, count);
    }

    public String getTopicName(String applicationName) {
        return super.getTableName(applicationName);
    }

    @Override
    public String getTableName(String applicationName) {
        throw new UnsupportedOperationException("getTableName method is not supported in this class");
    }
}