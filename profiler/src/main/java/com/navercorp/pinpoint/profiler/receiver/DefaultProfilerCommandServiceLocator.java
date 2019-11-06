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

import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DefaultProfilerCommandServiceLocator implements ProfilerCommandServiceLocator {

    private final IntHashMap<ProfilerCommandService> profilerCommandServiceRepository;
    private final Set<Short> codeSet;

    DefaultProfilerCommandServiceLocator(ProfilerCommandLocatorBuilder builder) {
        Map<Short, ProfilerCommandService> commandServiceRepository = builder.getProfilerCommandServiceRepository();
        this.profilerCommandServiceRepository = IntHashMapUtils.copyShortMap(commandServiceRepository);
        this.codeSet = buildCodeSet(commandServiceRepository);
    }

    @Override
    public ProfilerCommandService getService(short commandCode) {
        return profilerCommandServiceRepository.get(commandCode);
    }

    @Override
    public ProfilerSimpleCommandService getSimpleService(short commandCode) {

        final ProfilerCommandService service = profilerCommandServiceRepository.get(commandCode);
        if (service instanceof ProfilerSimpleCommandService) {
            return (ProfilerSimpleCommandService) service;
        }

        return null;
    }

    @Override
    public ProfilerRequestCommandService getRequestService(short commandCode) {

        final ProfilerCommandService service = profilerCommandServiceRepository.get(commandCode);
        if (service instanceof ProfilerRequestCommandService) {
            return (ProfilerRequestCommandService) service;
        }

        return null;
    }

    @Override
    public ProfilerStreamCommandService getStreamService(short commandCode) {

        final ProfilerCommandService service = profilerCommandServiceRepository.get(commandCode);
        if (service instanceof ProfilerStreamCommandService) {
            return (ProfilerStreamCommandService) service;
        }

        return null;
    }

    int getCommandServiceSize() {
        return profilerCommandServiceRepository.size();
    }

    @Override
    public Set<Short> getCommandServiceCodes() {
        return this.codeSet;
    }

    private Set<Short> buildCodeSet(Map<Short, ProfilerCommandService> codes) {
        return new HashSet<Short>(codes.keySet());

    }

}
