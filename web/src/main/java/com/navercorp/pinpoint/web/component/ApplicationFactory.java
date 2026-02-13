/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.component;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;

/**
 * @author emeroad
 */
public interface ApplicationFactory {

    Application createApplication(Service service, String applicationName, ServiceType serviceType);

    Application createApplication(Service service, String applicationName, int serviceType);

    Application createApplication(int serviceUid, String applicationName, int serviceTypeCode);


    Application createApplicationByTypeName(Service service, String applicationName, String serviceTypeName);
    Application createApplicationByTypeName(int serviceUid, String applicationName, String serviceTypeName);


    Application createApplication(String applicationName, int serviceTypeCode);

    Application createApplication(String applicationName, ServiceType serviceType);

    Application createApplicationByTypeName(String applicationName, String serviceTypeName);
}
