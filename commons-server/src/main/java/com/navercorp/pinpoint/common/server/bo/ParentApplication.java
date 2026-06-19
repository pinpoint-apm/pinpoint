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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * Parent application information of a {@link SpanBo}.
 * Present only for non-root spans; {@code serviceName} is optional.
 */
public class ParentApplication {

    private final String serviceName;
    private final String applicationName;
    private final int applicationServiceType;

    public ParentApplication(String applicationName, int applicationServiceType) {
        this(ServiceUid.DEFAULT_SERVICE_UID_NAME, applicationName, applicationServiceType);
    }

    public ParentApplication(String serviceName, String applicationName, int applicationServiceType) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.applicationServiceType = applicationServiceType;
    }

    /**
     * Builds a {@link ParentApplication}, substituting {@link ServiceUid#DEFAULT_SERVICE_UID_NAME}
     * when {@code serviceName} is absent (null or empty).
     */
    public static ParentApplication of(String serviceName, String applicationName, int applicationServiceType) {
        if (StringUtils.isEmpty(serviceName)) {
            return new ParentApplication(applicationName, applicationServiceType);
        }
        return new ParentApplication(serviceName, applicationName, applicationServiceType);
    }

    public String serviceName() {
        return serviceName;
    }

    public String applicationName() {
        return applicationName;
    }

    public int applicationServiceType() {
        return applicationServiceType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ParentApplication that = (ParentApplication) o;
        return applicationServiceType == that.applicationServiceType && serviceName.equals(that.serviceName) && applicationName.equals(that.applicationName);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + applicationServiceType;
        return result;
    }

    @Override
    public String toString() {
        return serviceName + '/' + applicationName + '/' + applicationServiceType;
    }
}
