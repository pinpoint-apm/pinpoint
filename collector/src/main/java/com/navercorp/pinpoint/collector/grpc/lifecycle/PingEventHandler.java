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

package com.navercorp.pinpoint.collector.grpc.lifecycle;

import com.navercorp.pinpoint.grpc.Header;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public interface PingEventHandler {

    PingSession newPingSession(Long id, Header header);

    void ping(PingSession pingSession);

    void close(PingSession pingSession);

    void update(PingSession pingSession);

    void update(Long id);
}
