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

import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.TBase;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DefaultProfilerCommandServiceLocator implements ProfilerCommandServiceLocator {

    private  final Map<Class<? extends TBase>, ProfilerCommandService> profilerCommandServiceRepository;

    DefaultProfilerCommandServiceLocator(ProfilerCommandLocatorBuilder builder) {
        this.profilerCommandServiceRepository = Collections.unmodifiableMap(builder.getProfilerCommandServiceRepository());
    }

    @Override
    public ProfilerCommandService getService(TBase tBase) {
        if (tBase == null) {
            return null;
        }
        return profilerCommandServiceRepository.get(tBase.getClass());
    }

    @Override
    public ProfilerSimpleCommandService getSimpleService(TBase tBase) {
        if (tBase == null) {
            return null;
        }

        final ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
        if (service instanceof ProfilerSimpleCommandService) {
            return (ProfilerSimpleCommandService) service;
        }

        return null;
    }

    @Override
    public ProfilerRequestCommandService getRequestService(TBase tBase) {
        if (tBase == null) {
            return null;
        }

        final ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
        if (service instanceof ProfilerRequestCommandService) {
            return (ProfilerRequestCommandService) service;
        }

        return null;
    }

    @Override
    public ProfilerStreamCommandService getStreamService(TBase tBase) {
        if (tBase == null) {
            return null;
        }

        final ProfilerCommandService service = profilerCommandServiceRepository.get(tBase.getClass());
        if (service instanceof ProfilerStreamCommandService) {
            return (ProfilerStreamCommandService) service;
        }

        return null;
    }

    @Override
    public Set<Class<? extends TBase>> getCommandServiceClasses() {
        return profilerCommandServiceRepository.keySet();
    }

    @Override
    public Set<Short> getCommandServiceCodes() {
        Set<Short> commandServiceCodes = new HashSet<Short>(profilerCommandServiceRepository.size());

        Set<Class<? extends TBase>> clazzSet = profilerCommandServiceRepository.keySet();
        for (Class<? extends TBase> clazz : clazzSet) {
            TCommandType commandType = TCommandType.getType(clazz);
            if (commandType != null) {
                commandServiceCodes.add(commandType.getCode());
            }
        }
        return commandServiceCodes;
    }

}
