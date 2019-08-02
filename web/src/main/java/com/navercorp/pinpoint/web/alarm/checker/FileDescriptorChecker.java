/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.FileDescriptorDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.Map;

/**
 * @author minwoo.jung
 */
public class FileDescriptorChecker extends AgentChecker<Long> {

    public FileDescriptorChecker(FileDescriptorDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected Map<String, Long> getAgentValues() {
        return ((FileDescriptorDataCollector)dataCollector).getFileDescriptorCount();
    }

    @Override
    protected boolean decideResult(Long value) {
        return value >= rule.getThreshold();
    }
}
