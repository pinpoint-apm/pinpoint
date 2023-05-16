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

import com.navercorp.pinpoint.profiler.receiver.grpc.ProfilerGrpcCommandService;

import java.util.Set;

/**
 * @author koo.taejin
 */
public interface ProfilerCommandServiceLocator<REQ, RES> {

    ProfilerCommandService getService(short commandCode);

    ProfilerSimpleCommandService<REQ> getSimpleService(short commandCode);

    ProfilerRequestCommandService<REQ, RES> getRequestService(short commandCode);

    ProfilerStreamCommandService<REQ> getStreamService(short commandCode);

    ProfilerGrpcCommandService getGrpcService(short commandCode);

    Set<Short> getCommandServiceCodes();

}
