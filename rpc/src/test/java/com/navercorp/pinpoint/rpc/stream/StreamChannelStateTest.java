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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StreamChannelStateTest {

    @Test
    public void functionTest1() {
        StreamChannelState state = new StreamChannelState();
        Assertions.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        state.to(StreamChannelStateCode.OPEN);
        Assertions.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());

        state.to(StreamChannelStateCode.CONNECT_AWAIT);
        Assertions.assertEquals(StreamChannelStateCode.CONNECT_AWAIT, state.getCurrentState());

        state.to(StreamChannelStateCode.CONNECTED);
        Assertions.assertEquals(StreamChannelStateCode.CONNECTED, state.getCurrentState());

        state.to(StreamChannelStateCode.CLOSED);
        Assertions.assertEquals(StreamChannelStateCode.CLOSED, state.getCurrentState());
    }

    @Test
    public void functionTest2() {
        StreamChannelState state = new StreamChannelState();
        Assertions.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        state.to(StreamChannelStateCode.OPEN);
        Assertions.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());

        state.to(StreamChannelStateCode.CONNECT_ARRIVED);
        Assertions.assertEquals(StreamChannelStateCode.CONNECT_ARRIVED, state.getCurrentState());

        state.to(StreamChannelStateCode.CONNECTED);
        Assertions.assertEquals(StreamChannelStateCode.CONNECTED, state.getCurrentState());

        state.to(StreamChannelStateCode.CLOSED);
        Assertions.assertEquals(StreamChannelStateCode.CLOSED, state.getCurrentState());
    }

    @Test
    public void functionTest3() {
        StreamChannelState state = new StreamChannelState();
        Assertions.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        boolean result = state.to(StreamChannelStateCode.CONNECTED);
        Assertions.assertFalse(result);
    }

    @Test
    public void functionTest4() {
        StreamChannelState state = new StreamChannelState();
        Assertions.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        state.to(StreamChannelStateCode.OPEN);
        Assertions.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());

        boolean result = state.to(StreamChannelStateCode.CONNECTED);
        Assertions.assertFalse(result);
    }

}
