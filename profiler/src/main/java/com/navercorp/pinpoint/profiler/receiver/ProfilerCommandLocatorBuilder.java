/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ProfilerCommandLocatorBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Short, ProfilerCommandService> profilerCommandServiceRepository;

    public ProfilerCommandLocatorBuilder() {
        this.profilerCommandServiceRepository = new HashMap<Short, ProfilerCommandService>();
    }

    public void addService(ProfilerCommandServiceGroup serviceGroup) {
        if (serviceGroup == null) {
            throw new NullPointerException("serviceGroup");
        }

        for (ProfilerCommandService service : serviceGroup.getCommandServiceList()) {
            addService(service);
        }
    }

    public boolean addService(ProfilerCommandService service) {
        if (service == null) {
            throw new NullPointerException("service");
        }
        return addService(service.getCommandServiceCode(), service);
    }

    boolean addService(short commandCode, ProfilerCommandService service) {
        if (service == null) {
            throw new NullPointerException("service");
        }

        final ProfilerCommandService exist = profilerCommandServiceRepository.get(commandCode);
        if (exist != null) {
            logger.warn("Already Register CommandCode:{}, RegisteredService:{}.", commandCode, exist);
            return false;
        }

        profilerCommandServiceRepository.put(commandCode, service);
        return true;
    }

    public ProfilerCommandServiceLocator build() {
        return new DefaultProfilerCommandServiceLocator(this);
    }

    protected Map<Short, ProfilerCommandService> getProfilerCommandServiceRepository() {
        return profilerCommandServiceRepository;
    }

}
