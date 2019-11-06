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

package com.navercorp.pinpoint.rpc.server;

/**
 * @author Taejin Koo
 */
public class HealthCheckStateContext {

    private volatile HealthCheckState state = HealthCheckState.WAIT;

    public HealthCheckState getState() {
        return state;
    }

    void toReceived() {
        final HealthCheckState state = this.state;

        if (state == HealthCheckState.WAIT) {
            this.state = HealthCheckState.RECEIVED;
        } else {
            throw new IllegalStateException("Illegal State. current:" + state + ", next:RECEIVED");
        }
    }

    void toReceivedLegacy() {
        final HealthCheckState state = this.state;

        if (state == HealthCheckState.WAIT) {
            this.state = HealthCheckState.RECEIVED_LEGACY;
        } else {
            throw new IllegalStateException("Illegal State. current:" + state + ", next:RECEIVED_LEGACY");
        }
    }

}
