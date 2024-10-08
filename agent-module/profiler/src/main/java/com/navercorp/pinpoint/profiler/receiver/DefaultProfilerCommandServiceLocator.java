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
import com.navercorp.pinpoint.profiler.receiver.grpc.ProfilerGrpcCommandService;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public class DefaultProfilerCommandServiceLocator implements ProfilerCommandServiceLocator {

    private final IntHashMap<ProfilerCommandService> profilerCommandServiceRepository;
    private final Set<Short> codeSet;

    DefaultProfilerCommandServiceLocator(Map<Short, ProfilerCommandService> commandServiceRepository) {
        Objects.requireNonNull(commandServiceRepository, "commandServiceRepository");
        this.profilerCommandServiceRepository = IntHashMapUtils.copyShortMap(commandServiceRepository);
        this.codeSet = buildCodeSet(commandServiceRepository);
    }

    @Override
    public ProfilerCommandService getService(short commandCode) {
        return profilerCommandServiceRepository.get(commandCode);
    }


    @Override
    public ProfilerGrpcCommandService getGrpcService(short commandCode) {

        final ProfilerCommandService service = profilerCommandServiceRepository.get(commandCode);
        if (service instanceof ProfilerGrpcCommandService) {
            return (ProfilerGrpcCommandService) service;
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
        return new HashSet<>(codes.keySet());

    }

}
