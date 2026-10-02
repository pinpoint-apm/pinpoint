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
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.dao.ApplicationDao;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import com.navercorp.pinpoint.service.service.ServiceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ApplicationIndexExistenceChecker {

    private final ApplicationDao applicationDao;
    private final ServiceModelResolver serviceModelResolver;

    public ApplicationIndexExistenceChecker(ApplicationDao applicationDao,
                                            ServiceModelResolver serviceModelResolver) {
        this.applicationDao = Objects.requireNonNull(applicationDao, "applicationDao");
        this.serviceModelResolver =
                Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
    }

    public String category() {
        return AlarmDataSource.APM_CATEGORY;
    }

    public boolean exists(AlarmApplication application) {
        // The row key pads the name to a fixed width and throws past it, and the request's own
        // limit counts characters rather than bytes, so a long multi-byte name reaches here.
        // No such name is in the index, which is the answer either way.
        if (BytesUtils.toBytes(application.getApplicationName()).length
                > PinpointConstants.APPLICATION_NAME_MAX_LEN_V3) {
            return false;
        }

        final Service service;
        try {
            service = serviceModelResolver.getService(application.getServiceName());
        } catch (ServiceNotFoundException e) {
            // A service the registry does not know owns no applications.
            return false;
        }

        List<Application> indexed =
                applicationDao.getApplications(service.getServiceUid(), application.getApplicationName());
        for (Application candidate : indexed) {
            // An indexed row whose type code no loaded plugin claims reads as UNDEFINED, and a
            // rule naming that back would evaluate to nothing every cycle: the batch raises on
            // it rather than reading an empty histogram.
            if (ServiceType.UNDEFINED.equals(candidate.getServiceType())) {
                continue;
            }
            if (candidate.getServiceType().getName().equals(application.getApplicationType())) {
                return true;
            }
        }
        return false;
    }
}
