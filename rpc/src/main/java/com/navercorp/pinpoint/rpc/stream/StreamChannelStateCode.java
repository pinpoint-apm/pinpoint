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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author koo.taejin
 */
public enum StreamChannelStateCode {

    NEW,
    OPEN(NEW),
    OPEN_AWAIT(OPEN),
    OPEN_ARRIVED(OPEN),
    RUN(OPEN_AWAIT, OPEN_ARRIVED),
    CLOSED(OPEN_AWAIT, OPEN_ARRIVED, RUN),
    ILLEGAL_STATE(NEW, OPEN, OPEN_AWAIT, OPEN_ARRIVED, RUN, CLOSED);

    private final Set<StreamChannelStateCode> validBeforeStateSet;

    private StreamChannelStateCode(StreamChannelStateCode... validBeforeStates) {
        this.validBeforeStateSet = new HashSet<StreamChannelStateCode>();

        if (validBeforeStates != null) {
            Collections.addAll(validBeforeStateSet, validBeforeStates);
        }
    }

    public boolean canChangeState(StreamChannelStateCode currentState) {
        if (validBeforeStateSet.contains(currentState)) {
            return true;
        }

        return false;
    }

}
