/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class ProfilerCommandLocatorBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Class<? extends TBase>, ProfilerCommandService> profilerCommandServiceRepository;

    public ProfilerCommandLocatorBuilder() {
        profilerCommandServiceRepository = new HashMap<Class<? extends TBase>, ProfilerCommandService>();
    }

    public void addService(ProfilerCommandServiceGroup serviceGroup) {
        if (serviceGroup == null) {
            throw new NullPointerException("serviceGroup must not be null");
        }

        for (ProfilerCommandService service : serviceGroup.getCommandServiceList()) {
            addService(service);
        }
    }

    public boolean addService(ProfilerCommandService service) {
        if (service == null) {
            throw new NullPointerException("service must not be null");
        }
        return addService(service.getCommandClazz(), service);
    }

    public boolean addService(Class<? extends TBase> clazz, ProfilerCommandService service) {
        if (clazz == null) {
            throw new NullPointerException("clazz must not be null");
        }
        if (service == null) {
            throw new NullPointerException("service must not be null");
        }

        boolean hasValue = profilerCommandServiceRepository.containsKey(clazz);
        if (!hasValue) {
            profilerCommandServiceRepository.put(clazz, service);
            return true;
        } else {
            ProfilerCommandService registeredService = profilerCommandServiceRepository.get(clazz);
            logger.warn("Already Register ServiceTypeInfo:{}, RegisteredService:{}.", clazz.getName(), registeredService);
            return false;
        }
    }

    public ProfilerCommandServiceLocator build() {
        return new DefaultProfilerCommandServiceLocator(this);
    }

    protected Map<Class<? extends TBase>, ProfilerCommandService> getProfilerCommandServiceRepository() {
        return profilerCommandServiceRepository;
    }

}
