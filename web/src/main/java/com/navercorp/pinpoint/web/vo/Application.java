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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
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
    private final Service service;

    private final String applicationName;
    private final ServiceType serviceType;

    public Application(String applicationName, ServiceType serviceType) {
        this(Service.DEFAULT, applicationName, serviceType);
    }

    public Application(Service service, String applicationName, ServiceType serviceType) {
        this.service = Objects.requireNonNull(service, "service");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    public Service getService() {
        return service;
    }

    public String getName() {
        return applicationName;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public int getServiceTypeCode() {
        return serviceType.getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;
        return Objects.equals(service, that.service) && Objects.equals(applicationName, that.applicationName) && Objects.equals(serviceType, that.serviceType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(service);
        result = 31 * result + Objects.hashCode(applicationName);
        result = 31 * result + Objects.hashCode(serviceType);
        return result;
    }

    public boolean equals(Service service, String name, int serviceTypeCode) {
        return this.service.equals(service) &&  this.applicationName.equals(name) && this.serviceType.getCode() == serviceTypeCode;
    }

    public boolean equals(String name, int serviceTypeCode) {
        return this.equals(Service.DEFAULT, name, serviceTypeCode);
    }

    @Override
    public String toString() {
        return service.getServiceName() + "(" + service.getUid() + ")/"
                + applicationName + "(" + serviceType.getDesc() + ":" + serviceType.getCode() + ")";
    }
}
