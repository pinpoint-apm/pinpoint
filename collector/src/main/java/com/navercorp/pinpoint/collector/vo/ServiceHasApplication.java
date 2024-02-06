/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.vo;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public record ServiceHasApplication(Long serviceId, Long applicationId) {

    public ServiceHasApplication(Long serviceId, Long applicationId) {
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
        this.applicationId = Objects.requireNonNull(applicationId, "applicationId");
    }
}