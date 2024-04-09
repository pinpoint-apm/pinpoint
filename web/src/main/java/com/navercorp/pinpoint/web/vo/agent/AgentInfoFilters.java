/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo.agent;

/**
 * @author youngjin.kim2
 */
public class AgentInfoFilters {

    private static final AgentInfoFilter ACCEPT_ALL = new AcceptAll();

    public static AgentInfoFilter acceptAll() {
        return ACCEPT_ALL;
    }

    public static AgentInfoFilter exactServiceTypeName(String serviceTypeName) {
        if (serviceTypeName == null) {
            return ACCEPT_ALL;
        }
        return new ExactServiceTypeName(serviceTypeName);
    }

    private static class AcceptAll implements AgentInfoFilter {
        @Override
        public boolean test(AgentInfo agentInfo) {
            return true;
        }
    }

    private record ExactServiceTypeName(String serviceTypeName) implements AgentInfoFilter {
        @Override
        public boolean test(AgentInfo agentInfo) {
            if (agentInfo == null) {
                return false;
            }
            return agentInfo.getServiceType().getName().equals(serviceTypeName);
        }
    }

}
