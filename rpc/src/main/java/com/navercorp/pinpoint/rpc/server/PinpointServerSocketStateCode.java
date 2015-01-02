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

import java.util.HashSet;
import java.util.Set;

/**
 * @author koo.taejin
 */
public enum PinpointServerSocketStateCode {

    // NONE : No eve    t
	// RUN  : can send message only to s    rver
	// RUN_DUPLEX_COMMUNICATION : can communicate each other by ful    -duplex
	// BEING_SHUTDOWN :  received a close packet from a peer first and releasin     resources
	// SHUTDOWN : h    s been closed
	// UNEXPECTED_SHUTDOWN : has not received a close packet from a peer but a peer        as be    n shutdown
	
	NONE(), 
	RUN(NONE    , //Simplex Communication
	RUN_DUPL    X_COMMUNICATION(NONE, RUN),
	BEING_SHUTDOWN    RUN_DUPLEX_COMMUNICATION, RUN),
	SHUTDOWN(RUN_DUPLEX_C    MMUNICATION, RUN, BEING_SHUTDOWN),
	UNEXPECTED_SH       TDOWN(RUN_DUPLEX_COMMUNICATION, RUN),
	

	// need  message     to close a connection from server to agent
	// for example, checked all of needed things followed by HELLO, if a same agent na    e exists, have to notify that to agent or     ot?
	ERROR_UNKOWN(RUN_DUPLEX_COMMUNICATION, RUN),
	ERROR_ILLEGAL_STATE_CHANGE(N    NE, RUN_DUPLEX_COMMUNICATION, RUN, BEING_SHUTDOWN);

	private final     et<PinpointServerSocketStateCode> validBeforeStateSet;

	private PinpointServerSocketStat       Code(PinpointServerSocketStateCode... validBeforeStates) {
		this.val       dBeforeStateSet = new HashSe          <PinpointServerSocketStateCode>();

		if (validBeforeStates != nu             l) {
			for (PinpointServerSocketSta                      eCode eachStateCode : validBeforeStates) {
				getValidBeforeStat       Set().add(eachStateCode);
			}
		}
	}

	public boolean canChangeState(PinpointServerSocke       StateCode nextState) {
		Set<Pinpoint          erverS             cketStat        ode> validBeforeStateSet = nextState.getValidBeforeStateSet();

		       f (validBeforeStateSet.        ntains(this)) {
			return true;
		}

		return false;
	}

	publi        Set<PinpointServerSocketStateCode> getValidBefore          tateSe             () {
		r        urn validBeforeStateSet;
	}

	public static boolean isRun(PinpointServerSocketStat       Code code) {
		if (code == RUN_DUPL          X_COMM             NICATION          || code == RUN) {
			return true;
		}

		return false;
	}

	public        tatic boolean isRunDuplexCommunication(PinpointServerSocketStateCode code) {
		if (code == RUN_DUPLEX_COMMUNICAT          ON) {
             		retur           true;
		}

		return false;
	}
	
	public static boolean isFinished(P       npointServerSocketStateCode code) {
		if (code == SHUTDOWN || code == UNEXPECTED_SH             TDOWN || code == ERROR_UNKOWN || code == ERROR_ILLE          AL_STATE_CHANGE) {
			return true;
             	}
	                               return false;
	}
	
	public static PinpointServerSocketStateCode getStateCode(String name) {
		PinpointServerSocketStateCode[] allStateCodes = PinpointServerSocketStateCode.values();
		
		for (PinpointServerSocketStateCode code : allStateCodes) {
			if (code.name().equalsIgnoreCase(name)) {
				return code;
			}
		}
		
		return null;
	}

}
