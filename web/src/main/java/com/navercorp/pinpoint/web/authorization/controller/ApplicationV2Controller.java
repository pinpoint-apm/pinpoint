/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.service.ApplicationServiceV2;
import com.navercorp.pinpoint.web.uid.service.ServiceUidService;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/application/v2")
@Validated
@ConditionalOnProperty(name = "pinpoint.web.application.index.v2.enabled", havingValue = "true")
public class ApplicationV2Controller {

    private final ServiceUidService serviceUidService;
    private final ApplicationServiceV2 applicationServiceV2;

    public ApplicationV2Controller(@Autowired(required = false) ServiceUidService serviceUidService,
                                   ApplicationServiceV2 applicationServiceV2) {
        this.serviceUidService = serviceUidService;
        this.applicationServiceV2 = Objects.requireNonNull(applicationServiceV2, "applicationServiceV2");
    }

    @GetMapping()
    public List<Application> getApplicationListV2(
            @RequestParam(value = "serviceName", required = false) String serviceName) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        return applicationServiceV2.getApplications(serviceUid);
    }

    @GetMapping(params = {"applicationName"})
    public List<Application> getApplicationListV2(
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam(value = "applicationName") String applicationName) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        return applicationServiceV2.getApplications(serviceUid, applicationName);
    }


    //delete

    //exist

    private ServiceUid handleServiceUid(String serviceName) {
        if (serviceUidService == null || StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        ServiceUid serviceUid = serviceUidService.getServiceUid(serviceName);
        if (serviceUid == null) {
            throw new IllegalArgumentException("service not found. name: " + serviceName);
        }
        return serviceUid;
    }
}
