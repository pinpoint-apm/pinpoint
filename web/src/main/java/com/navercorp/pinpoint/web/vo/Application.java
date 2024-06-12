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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.view.ApplicationSerializer;

import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
@JsonSerialize(using = ApplicationSerializer.class)
public record Application(ApplicationId id, String name, ServiceType serviceType) {

    public Application(ApplicationId id, String name, ServiceType serviceType) {
        this.id = id;
        this.name = name;
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    public Application(String name, ServiceType serviceType) {
        this(null, name, serviceType);
    }

    public short getServiceTypeCode() {
        return serviceType.getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(serviceType, that.serviceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, serviceType);
    }

    @Override
    public String toString() {
        return name + "(" + serviceType.getDesc() + ":" + serviceType.getCode() + ")";
    }
}
