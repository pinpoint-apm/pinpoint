/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.PinpointSocketException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author koo.taejin
 */
public class StreamChannelState {

    private final AtomicReference<StreamChannelStateCode> currentStateReference = new AtomicReference<StreamChannelStateCode>();

    public StreamChannelState() {
        currentStateReference.set(StreamChannelStateCode.NEW);
    }

    public StreamChannelStateCode getCurrentState() {
        return currentStateReference.get();
    }

    boolean to(StreamChannelStateCode nextState) {
        return to(currentStateReference.get(), nextState);
    }

    boolean to(StreamChannelStateCode currentState, StreamChannelStateCode nextState) {
        if (!nextState.canChangeState(currentState)) {
            return false;
        }

        boolean isChanged = currentStateReference.compareAndSet(currentState, nextState);
        return isChanged;
    }

    public boolean checkState(StreamChannelStateCode expectedCode) {
        return checkState(getCurrentState(), expectedCode);
    }

    public boolean checkState(StreamChannelStateCode currentCode, StreamChannelStateCode expectedCode) {
        if (currentCode == expectedCode) {
            return true;
        } else {
            return false;
        }
    }

    public void assertState(StreamChannelStateCode stateCode) {
        final StreamChannelStateCode currentCode = getCurrentState();
        if (!checkState(currentCode, stateCode)) {
            throw new PinpointSocketException("expected:<" + stateCode + "> but was:<" + currentCode + ">;");
        }
    }

    @Override
    public String toString() {
        return currentStateReference.get().name();
    }

}
