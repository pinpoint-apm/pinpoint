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

package com.navercorp.pinpoint.rpc.server;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.rpc.server.PinpointServerState;
import com.navercorp.pinpoint.rpc.server.PinpointServerStateCode;

/**
 * @author koo.taejin
 */
public class PinpointServerSocketStateTest {

    // basic type of connection's lifecycle between peers.
    // RUN -> RUN_DUPLEX_COMMUNICATION ->  BEING_SHUTDOWN -> connection closed
    @Test
    public void changeStateTest1() {
        PinpointServerState state = new PinpointServerState();

        state.changeState(PinpointServerStateCode.RUN_WITHOUT_HANDSHAKE);
        Assert.assertEquals(PinpointServerStateCode.RUN_WITHOUT_HANDSHAKE, state.getCurrentState());

        state.changeState(PinpointServerStateCode.RUN_DUPLEX);
        Assert.assertEquals(PinpointServerStateCode.RUN_DUPLEX, state.getCurrentState());

        state.changeState(PinpointServerStateCode.BEING_SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.BEING_SHUTDOWN, state.getCurrentState());

        state.changeState(PinpointServerStateCode.SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.SHUTDOWN, state.getCurrentState());
    }

    // basic type of connection's lifecycle between peers.
    // RUN_DUPLEX_COMMUNICATION -> RUN_DUPLEX_COMMUNICATION -> BEING_SHUTDOWN -> connection closed
    @Test
    public void changeStateTest2() {
        PinpointServerState state = new PinpointServerState();

        state.changeState(PinpointServerStateCode.RUN_DUPLEX);
        Assert.assertEquals(PinpointServerStateCode.RUN_DUPLEX, state.getCurrentState());

        PinpointServerStateCode currentState = state.changeState(PinpointServerStateCode.BEING_SHUTDOWN);
        Assert.assertNull(currentState);
        Assert.assertEquals(PinpointServerStateCode.BEING_SHUTDOWN, state.getCurrentState());

        currentState = state.changeState(PinpointServerStateCode.BEING_SHUTDOWN, PinpointServerStateCode.BEING_SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.BEING_SHUTDOWN, currentState);
        Assert.assertEquals(PinpointServerStateCode.BEING_SHUTDOWN, state.getCurrentState());

        state.changeState(PinpointServerStateCode.SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.SHUTDOWN, state.getCurrentState());
    }

    @Test
    public void changeStateTest3() {
        PinpointServerState state = new PinpointServerState();

        state.changeState(PinpointServerStateCode.RUN_WITHOUT_HANDSHAKE);
        Assert.assertEquals(PinpointServerStateCode.RUN_WITHOUT_HANDSHAKE, state.getCurrentState());

        state.changeState(PinpointServerStateCode.UNEXPECTED_SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.UNEXPECTED_SHUTDOWN, state.getCurrentState());
    }

    @Test
    public void changeStateTest4() {
        PinpointServerState state = new PinpointServerState();

        state.changeState(PinpointServerStateCode.RUN_WITHOUT_HANDSHAKE);
        Assert.assertEquals(PinpointServerStateCode.RUN_WITHOUT_HANDSHAKE, state.getCurrentState());

        state.changeState(PinpointServerStateCode.SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.SHUTDOWN, state.getCurrentState());
    }

    @Test
    public void changeStateTest5() {
        PinpointServerState state = new PinpointServerState();

        state.changeState(PinpointServerStateCode.RUN_DUPLEX);
        Assert.assertEquals(PinpointServerStateCode.RUN_DUPLEX, state.getCurrentState());

        state.changeState(PinpointServerStateCode.SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.SHUTDOWN, state.getCurrentState());
    }

    @Test
    public void invalidChangeStateTest1() {
        PinpointServerState state = new PinpointServerState();
        PinpointServerStateCode beforeCode = state.changeState(PinpointServerStateCode.BEING_SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.NONE, beforeCode);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidChangeStateTest2() {
        PinpointServerState state = new PinpointServerState();
        state.changeStateThrowWhenFailed(PinpointServerStateCode.BEING_SHUTDOWN);
    }
    

    @Test
    public void invalidChangeStateTest3() {
        PinpointServerState state = new PinpointServerState();

        state.changeState(PinpointServerStateCode.RUN_DUPLEX);
        Assert.assertEquals(PinpointServerStateCode.RUN_DUPLEX, state.getCurrentState());

        state.changeStateThrowWhenFailed(PinpointServerStateCode.BEING_SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.BEING_SHUTDOWN, state.getCurrentState());

        PinpointServerStateCode beforeCode = state.changeState(PinpointServerStateCode.UNEXPECTED_SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.BEING_SHUTDOWN, beforeCode);
    }

    @Test(expected = IllegalStateException.class)
    public void invalidChangeStateTest4() {
        PinpointServerState state = new PinpointServerState();

        state.changeState(PinpointServerStateCode.RUN_DUPLEX);
        Assert.assertEquals(PinpointServerStateCode.RUN_DUPLEX, state.getCurrentState());

        state.changeStateThrowWhenFailed(PinpointServerStateCode.BEING_SHUTDOWN);
        Assert.assertEquals(PinpointServerStateCode.BEING_SHUTDOWN, state.getCurrentState());

        PinpointServerStateCode beforeCode = state.changeStateThrowWhenFailed(PinpointServerStateCode.UNEXPECTED_SHUTDOWN);
    }

}
