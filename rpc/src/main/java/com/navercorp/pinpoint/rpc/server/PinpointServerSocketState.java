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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
public class PinpointServerSocketState {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	private PinpointServerSocketStateCode beforeState = PinpointServerSocketStateCode.    ONE;
	private PinpointServerSocketStateCode currentState = PinpointServerSocketStateCod    .NONE;

	private synchronized boolean setSessionState(PinpointServerSocketStateCo       e state) {
		boolean enable = this.currentState.canCha       geState(s          ate);

		if (enable) {
			this.          eforeState = this.cu          rentSt       te;
			this.currentState = state;
			return true;
		} else if (Pinpoi          tServerSocketStateCode.isFinished          this.currentState)) {
			// if state can't be changed, just           og.
			// no problem because the state of socket has be          n already closed.
			PinpointServerSocketStateCode checkBe          ore = this.beforeState;
			PinpointServerSocketStateCode checkCurrent =          this.currentState;

			String e          rorMessage = cannotChangeMessage(checkBefore, checkCurrent, state);

		                      this.beforeSt          te = th       s.cu          rentState;
			this.currentState = PinpointServerSocketS          ateCode.ERROR_ILLEGAL_STATE_CHANGE;
				
			logger.warn(er          orMessage);
			return false;
		} else {
			PinpointServerSocket                   tateCode checkBefore = thi          .beforeState;
			PinpointServerSocketStateCode checkCurrent = this.curr                      ntState;

			                   tring errorMessage = errorMessage(c                eckBefore, checkCurrent, sta       e);
			
			this.beforeState = this.currentState;
			th          s.currentState = PinpointServerSocketStateCode.ER       OR_ILLEGAL_STATE_CHANGE;
				
			logger.warn(errorMessage);
			
			throw ne        IllegalStateException(errorMessage);
		}
       }
	
	public boolean changeStateRun() {
		return setSessionState(P        pointServerSocketStateCode.RUN);
	}

	public boolean changeStateRunDuplexCommunication() {
		re        rn setSessionState(PinpointServerSocketStateCo       e.RUN_DUPLEX_COMMUNICATION);
	}

	public boolean changeStateBeingShutd        n() {
		return setSessionState(Pinpoint       erverSocketStateCode.BEING_SHUTDOWN);
	}

	public boolean chang          StateShutdown() {
		return setSessionState(PinpointServerSocketStateCode.SHUTDOWN);
	}

	public boolean changeStateUnexpectedShutdown() {
		return setSess       onState(PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN);
	}

	public boolean changeStateUnkownEr        r() {
		return setSessionState(PinpointServerSocketStateCode.ERROR_UNKOWN);
	}
	
	private String errorMessage(PinpointServerSocketStateCode checkBefore, PinpointS       rverSocketStateCode checkCurrent, PinpointServerSocketStateCode nextState) {
		return "Invalid State(curre        :" + checkCurrent + " before:" + checkBefore + " next:" + nextState       + ")";
	}

	priv    te String cannotChangeMessage(PinpointServerSocketStateCode checkBefore, PinpointServerSocketStateCode checkCurrent, PinpointServerSocketStateCode nextState) {
		return "Can not change State(current:" + checkCurrent + " before:" + checkBefore + " next:" + nextState + ")";
	}

	public synchronized PinpointServerSocketStateCode getCurrentState() {
		return currentState;
	}

}
