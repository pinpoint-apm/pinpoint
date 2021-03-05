/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.grpc;

import java.util.Objects;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import io.grpc.ManagedChannel;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CommandServiceStubFactory {
    private final ManagedChannel managedChannel;

    public CommandServiceStubFactory(ManagedChannel managedChannel) {
        this.managedChannel = Objects.requireNonNull(managedChannel, "managedChannel");
    }

    public ProfilerCommandServiceGrpc.ProfilerCommandServiceStub newStub() {
        return ProfilerCommandServiceGrpc.newStub(managedChannel);
    }
}
