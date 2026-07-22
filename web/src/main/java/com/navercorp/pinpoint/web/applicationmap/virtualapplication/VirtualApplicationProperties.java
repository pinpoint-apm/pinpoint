/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.virtualapplication;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "web.virtual-service")
public class VirtualApplicationProperties {

    private boolean mockEnabled = false;
    private List<VirtualApplicationRule> mappings = new ArrayList<>();

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public List<VirtualApplicationRule> getMappings() {
        return mappings;
    }

    public void setMappings(List<VirtualApplicationRule> mappings) {
        this.mappings = mappings;
    }

    public static class VirtualApplicationRule {
        private String virtualServiceName;
        private List<ApplicationRef> members = new ArrayList<>();

        public String getVirtualServiceName() {
            return virtualServiceName;
        }

        public void setVirtualServiceName(String virtualServiceName) {
            this.virtualServiceName = virtualServiceName;
        }

        public List<ApplicationRef> getMembers() {
            return members;
        }

        public void setMembers(List<ApplicationRef> members) {
            this.members = members;
        }
    }

    public static class ApplicationRef {
        private String name;
        private String serviceType;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }
    }
}