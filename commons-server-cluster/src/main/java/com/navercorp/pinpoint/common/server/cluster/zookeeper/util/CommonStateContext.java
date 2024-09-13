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

package com.navercorp.pinpoint.common.server.cluster.zookeeper.util;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class CommonStateContext {

    private static final AtomicReferenceFieldUpdater<CommonStateContext, CommonState> REF
            = AtomicReferenceFieldUpdater.newUpdater(CommonStateContext.class, CommonState.class, "currentState");

    private volatile CommonState currentState = null;

    public CommonStateContext() {
        REF.set(this, CommonState.NEW);
    }

    public CommonState getCurrentState() {
        return REF.get(this);
    }

    public boolean changeStateInitializing() {
        return REF.compareAndSet(this, CommonState.NEW, CommonState.INITIALIZING);
    }

    public boolean changeStateStarted() {
        return REF.compareAndSet(this, CommonState.INITIALIZING, CommonState.STARTED);
    }

    public boolean changeStateDestroying() {
        return REF.compareAndSet(this, CommonState.STARTED, CommonState.DESTROYING);
    }

    public boolean changeStateStopped() {
        return REF.compareAndSet(this, CommonState.DESTROYING, CommonState.STOPPED);
    }

    public boolean changeStateIllegal() {
        REF.set(this, CommonState.ILLEGAL_STATE);
        return true;
    }

    public boolean isStarted() {
        return REF.get(this) == CommonState.STARTED;
    }
}
