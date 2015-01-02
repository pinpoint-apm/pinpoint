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

import com.navercorp.pinpoint.rpc.server.PinpointServerSocketState;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocketStateCode;

/**
 * @author koo.taejin
 */
public class PinpointServerSocketStateTest {

    // basic type of connection's lifecycle between peer    .
	// RUN -> RUN_DUPLEX_COMMUNICATION ->  BEING_SHUTDOWN -> connection c    ose
	@Test
	public void changeSta       eTest1() {
		PinpointServerSocketState state = new PinpointServ       rSocketState();

		       tate.changeStateRun();
		Assert.assertEquals(PinpointServerSocketStateCode.RU       , state.getCurrentState());

		state.c       angeStateRunDuplexCommunication();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_       OMMUNICATION, state.getCurren       State());

		state.changeStateBeingShutdown();
		Assert.assertEquals(PinpointServerSocke       StateCode.BEING_SHUTDOWN        state.getCurrentState());

		state.changeStateShutdown();
		Assert.assertEquals(        npointServerSocketStateCode.SHUTDOWN, state.getCurre    tState());
	}

	// basic type of connection's lifecycle between peers.
	// RUN_DUPLEX_COMMUN    CAT    ON -> RUN_DUPLEX_COMMUNICATION       -> BEING_SHUTDOWN -> connection closed
	@Test
	public void chan       eStateTest2() {
		PinpointServerSocket       tate state = new PinpointServerSocketState();

		state.changeStateRunDuplexCommunication();
		Asse       t.assertEquals(PinpointServer       ocketStateCode.RUN_DUPLEX_COMMUNICATION, state.getCurrentState());

		state.changeStateB       ingShutdown();
		Assert.       ssertEquals(PinpointServerSocketStateCode.BEING_SHUTDOWN, state.getCurrentState()

	    state.changeStateShutdown();
	       Assert.assertEquals(PinpointServerSocketStateCode.SHUTDOWN, sta       e.getCurrentState()       ;
	}

	@Test
	public void changeStateTest3() {
		PinpointServerSocketState st       te = new PinpointServerSocketState       );

		state.changeStateRun();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN, state        etC    rrentState());

		state.change       tateUnexpectedShutdown();
		Assert.assertEquals(PinpointServerS       cketStateCode.UNEXP       CTED_SHUTDOWN, state.getCurrentState());
	}

	@Test
	public void changeStateT       st4() {
		PinpointServer       ocketState state = new PinpointServerSocketState();

		state.changeStateRun();
		        ser    .assertEquals(PinpointServerSo       ketStateCode.RUN, state.getCurrentState());

		state.changeStat       Shutdown();
		Assert.assertEquals(Pinp       intServerSocketStateCode.SHUTDOWN, state.getCurrentState());
	}

	@Test
	public void changeStateTe       t5() {
		PinpointServerS       cketState state = new PinpointServerSocketState();

		state.changeStateRunDuplexC        munication();
		Assert.assertEquals(Pinpoin    ServerSocketStateCode.RUN_DUPLEX_COMM       NICATION, state.getCurrentState());

		state.changeStateShutdow       ();
		Assert.assertEquals(Pin        intServerSocketStateCode.SHUTDOWN, state.ge    CurrentState());
	}

	@Test(expected         IllegalStateException.class)
	public void invalidChangeStateTe       t1() {
		PinpointServerSocketState sta       e = new PinpointServerSocketState();

		state.changeStateBeingShutdown();
	}

	@Test(expected = Il       egalStateException.class)
	pu       lic void invalidChangeStateTest2() {
		PinpointServerSocketState state = new PinpointSer       erSocketState();

		state.changeSt    teRunDuplexCommunication();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, state.getCurrentState());

		state.changeStateBeingShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.BEING_SHUTDOWN, state.getCurrentState());

		state.changeStateUnexpectedShutdown();
	}

}
