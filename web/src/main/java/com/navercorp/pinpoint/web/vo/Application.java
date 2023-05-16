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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.view.ApplicationSerializer;

import java.util.Objects;

/**
 * 
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 * 
 */
@JsonSerialize(using = ApplicationSerializer.class)
public final class Application {
    private final String name;
    private final ServiceType serviceType;

    public Application(String name, ServiceType serviceType) {
        this.name = Objects.requireNonNull(name, "name");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }


    public String getName() {
        return name;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public short getServiceTypeCode() {
        return serviceType.getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;

        if (!name.equals(that.name)) return false;
        return serviceType.equals(that.serviceType);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + serviceType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name + "(" + serviceType.getDesc() + ":" + serviceType.getCode() + ")";
    }
}
