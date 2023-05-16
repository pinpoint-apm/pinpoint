/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.dao.model;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class HostInfoSearchKey {

    private final String tenantId;
    private final String hostGroupName;

    public HostInfoSearchKey(String tenantId, String hostGroupName) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.hostGroupName = Objects.requireNonNull(hostGroupName, "hostGroupName");
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getHostGroupName() {
        return hostGroupName;
    }
}
