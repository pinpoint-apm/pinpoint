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
package com.navercorp.pinpoint.alarm.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The application an alarm rule targets, named by service, application name and
 * application type rather than by an id. The type decides which registry owns the
 * application, so a rule is not tied to any single registry's identifier space.
 *
 * @see com.navercorp.pinpoint.alarm.service.AlarmApplicationExistenceChecker
 */
public class AlarmApplication {

    public static final String TYPE_JAVASCRIPT = "javascript";

    @NotBlank(message = "serviceName must not be blank")
    @Size(max = 127, message = "serviceName is too long")
    private String serviceName;

    @NotBlank(message = "applicationName must not be blank")
    @Size(max = 127, message = "applicationName is too long")
    private String applicationName;

    @NotBlank(message = "applicationType must not be blank")
    @Size(max = 30, message = "applicationType is too long")
    private String applicationType;

    public AlarmApplication() {
    }

    public AlarmApplication(String serviceName, String applicationName, String applicationType) {
        this.serviceName = serviceName;
        this.applicationName = applicationName;
        this.applicationType = applicationType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    @Override
    public String toString() {
        return "AlarmApplication{serviceName='" + serviceName
                + "', applicationName='" + applicationName
                + "', applicationType='" + applicationType + "'}";
    }
}
