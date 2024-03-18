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

package com.navercorp.pinpoint.loader.plugins.trace.yaml;

/**
 * @author HyunGil Jeong
 */
public class ParsedServiceTypeProperty {

    private boolean terminal;
    private boolean queue;
    private boolean recordStatistics;
    private boolean includeDestinationId;
    private boolean alias;

    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isQueue() {
        return queue;
    }

    public void setQueue(boolean queue) {
        this.queue = queue;
    }

    public boolean isRecordStatistics() {
        return recordStatistics;
    }

    public void setRecordStatistics(boolean recordStatistics) {
        this.recordStatistics = recordStatistics;
    }

    public boolean isIncludeDestinationId() {
        return includeDestinationId;
    }

    public void setIncludeDestinationId(boolean includeDestinationId) {
        this.includeDestinationId = includeDestinationId;
    }

    public boolean isAlias() {
        return alias;
    }

    public void setAlias(boolean alias) {
        this.alias = alias;
    }
}
