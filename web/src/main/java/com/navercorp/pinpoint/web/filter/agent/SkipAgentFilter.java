/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter.agent;

/**
 * @author emeroad
 */
public class SkipAgentFilter implements AgentFilter {
    public static final AgentFilter SKIP_FILTER = new SkipAgentFilter();

    public SkipAgentFilter() {
    }

    @Override
    public boolean accept(String agentId) {
        return ACCEPT;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SkipAgentFilter{");
        sb.append('}');
        return sb.toString();
    }
}
