/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.common.server.util.concurrent;

import java.util.concurrent.atomic.AtomicReference;

public class CommonStateContext {

    private final AtomicReference<CommonState> currentState = new AtomicReference<CommonState>();

    public CommonStateContext() {
        currentState.set(CommonState.NEW);
    }

    public CommonState getCurrentState() {
        return currentState.get();
    }

    public boolean changeStateInitializing() {
        return currentState.compareAndSet(CommonState.NEW, CommonState.INITIALIZING);
    }

    public boolean changeStateStarted() {
        return currentState.compareAndSet(CommonState.INITIALIZING, CommonState.STARTED);
    }

    public boolean changeStateDestroying() {
        return currentState.compareAndSet(CommonState.STARTED, CommonState.DESTROYING);
    }

    public boolean changeStateStopped() {
        return currentState.compareAndSet(CommonState.DESTROYING, CommonState.STOPPED);
    }

    public boolean changeStateIllegal() {
        currentState.set(CommonState.ILLEGAL_STATE);
        return true;
    }

    public boolean isStarted() {
        return currentState.get() == CommonState.STARTED;
    }
}
