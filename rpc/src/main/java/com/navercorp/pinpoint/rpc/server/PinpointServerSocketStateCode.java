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

	// NONE : No event
	// RUN  : can send message only to server
	// RUN_DUPLEX_COMMUNICATION : can communicate each other by full-duplex
	// BEING_SHUTDOWN : waiting to close connection      CLOSE 등의 명령을 받고 연결을 종료를 대기하는 상태
	// SHUTDOWN : 내가 끊거나 종료대기가 되어있는 상태일때 종료  
	// UNEXPECTED_SHUTDOWN : CLOSE 등의 명령을 받지 못한 상태에서 상대방이 연결을 종료하였을떄 
	
	NONE(), 
	RUN(NONE), //Simplex Communication
	RUN_DUPLEX_COMMUNICATION(NONE, RUN), 
	BEING_SHUTDOWN(RUN_DUPLEX_COMMUNICATION, RUN),
	SHUTDOWN(RUN_DUPLEX_COMMUNICATION, RUN, BEING_SHUTDOWN),
	UNEXPECTED_SHUTDOWN(RUN_DUPLEX_COMMUNICATION, RUN),
	
	// 서버쪽에서 먼저 연결을 끊자는 메시지도 필요하다. 
	// 예를 들어 HELLO 이후 다 확인했는데, 같은 Agent명이 있으면(?) 이걸 사용자에게 말해야 할까? 아닐까? 알림 등
	ERROR_UNKOWN(RUN_DUPLEX_COMMUNICATION, RUN), 
	ERROR_ILLEGAL_STATE_CHANGE(NONE, RUN_DUPLEX_COMMUNICATION, RUN, BEING_SHUTDOWN);

	private final Set<PinpointServerSocketStateCode> validBeforeStateSet;

	private PinpointServerSocketStateCode(PinpointServerSocketStateCode... validBeforeStates) {
		this.validBeforeStateSet = new HashSet<PinpointServerSocketStateCode>();

		if (validBeforeStates != null) {
			for (PinpointServerSocketStateCode eachStateCode : validBeforeStates) {
				getValidBeforeStateSet().add(eachStateCode);
			}
		}
	}

	public boolean canChangeState(PinpointServerSocketStateCode nextState) {
		Set<PinpointServerSocketStateCode> validBeforeStateSet = nextState.getValidBeforeStateSet();

		if (validBeforeStateSet.contains(this)) {
			return true;
		}

		return false;
	}

	public Set<PinpointServerSocketStateCode> getValidBeforeStateSet() {
		return validBeforeStateSet;
	}

	public static boolean isRun(PinpointServerSocketStateCode code) {
		if (code == RUN_DUPLEX_COMMUNICATION || code == RUN) {
			return true;
		}

		return false;
	}

	public static boolean isRunDuplexCommunication(PinpointServerSocketStateCode code) {
		if (code == RUN_DUPLEX_COMMUNICATION) {
			return true;
		}

		return false;
	}
	
	public static boolean isFinished(PinpointServerSocketStateCode code) {
		if (code == SHUTDOWN || code == UNEXPECTED_SHUTDOWN || code == ERROR_UNKOWN || code == ERROR_ILLEGAL_STATE_CHANGE) {
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
