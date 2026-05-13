/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.servicemap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "web.servicemap")
public class ServiceMappingProperties {

    private boolean mockEnabled = false;
    private List<ServiceMappingRule> mockMappings = new ArrayList<>();

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public List<ServiceMappingRule> getMockMappings() {
        return mockMappings;
    }

    public void setMockMappings(List<ServiceMappingRule> mockMappings) {
        this.mockMappings = mockMappings;
    }

    public static class ServiceMappingRule {
        private List<String> prefix;
        private String serviceName;
        private int serviceUid;

        public List<String> getPrefix() {
            return prefix;
        }

        public void setPrefix(List<String> prefix) {
            this.prefix = prefix;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public int getServiceUid() {
            return serviceUid;
        }

        public void setServiceUid(int serviceUid) {
            this.serviceUid = serviceUid;
        }
    }
}
