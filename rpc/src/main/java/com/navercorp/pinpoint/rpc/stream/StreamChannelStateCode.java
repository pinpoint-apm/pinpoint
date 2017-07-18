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

import com.navercorp.pinpoint.common.util.ArrayUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author koo.taejin
 */
public enum StreamChannelStateCode {

    NEW,
    OPEN(NEW),
    CONNECT_AWAIT(OPEN),
    CONNECT_ARRIVED(OPEN),
    CONNECTED(CONNECT_AWAIT, CONNECT_ARRIVED),
    CLOSED(CONNECT_AWAIT, CONNECT_ARRIVED, CONNECTED),
    ILLEGAL_STATE(NEW, OPEN, CONNECT_AWAIT, CONNECT_ARRIVED, CONNECTED, CLOSED);

    private final Set<StreamChannelStateCode> validBeforeStateSet;

    StreamChannelStateCode(StreamChannelStateCode... validBeforeStates) {
        validBeforeStateSet = asSet(validBeforeStates);
    }

    private Set<StreamChannelStateCode> asSet(StreamChannelStateCode[] validBeforeStates) {
        if (ArrayUtils.isEmpty(validBeforeStates)) {
            return Collections.emptySet();
        } else {
            // Don't use EnumSet, Not initialized StreamStateCode,
            Set<StreamChannelStateCode> temp = new HashSet<StreamChannelStateCode>();
            Collections.addAll(temp, validBeforeStates);
            return temp;
        }
    }

    public boolean canChangeState(StreamChannelStateCode currentState) {
        if (validBeforeStateSet.contains(currentState)) {
            return true;
        }

        return false;
    }

}
