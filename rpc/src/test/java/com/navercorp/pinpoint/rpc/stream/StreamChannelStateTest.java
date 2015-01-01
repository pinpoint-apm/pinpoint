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

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.rpc.stream.StreamChannelState;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;

public class StreamChannelStateTest {

    @Test
    public void functionTest1() {
        StreamChannelState state = new StreamChannelState();
        Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        state.changeStateOpen();
        Assert.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());

        state.changeStateOpenAwait();
        Assert.assertEquals(StreamChannelStateCode.OPEN_AWAIT, state.getCurrentState());

        state.changeStateRun();
        Assert.assertEquals(StreamChannelStateCode.RUN, state.getCurrentState());

        state.changeStateClose();
        Assert.assertEquals(StreamChannelStateCode.CLOSED, state.getCurrentState());
    }

    @Test
    public void functionTest2() {
        StreamChannelState state = new StreamChannelState();
        Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        state.changeStateOpenArrived();
        Assert.assertEquals(StreamChannelStateCode.OPEN_ARRIVED, state.getCurrentState());

        state.changeStateRun();
        Assert.assertEquals(StreamChannelStateCode.RUN, state.getCurrentState());

        state.changeStateClose();
        Assert.assertEquals(StreamChannelStateCode.CLOSED, state.getCurrentState());
    }

    @Test
    public void functionTest3() {
        StreamChannelState state = new StreamChannelState();
        Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        boolean result = state.changeStateRun();
        Assert.assertFalse(result);
        Assert.assertEquals(StreamChannelStateCode.ILLEGAL_STATE, state.getCurrentState());
    }

    @Test
    public void functionTest4() {
        StreamChannelState state = new StreamChannelState();
        Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());

        state.changeStateOpen();
        Assert.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());

        boolean result = state.changeStateRun();
        Assert.assertFalse(result);
        Assert.assertEquals(StreamChannelStateCode.ILLEGAL_STATE, state.getCurrentState());
    }

}
