package com.nhn.pinpoint.rpc.server;

import java.util.HashSet;
import java.util.Set;

/**
 * @author koo.taejin
 */
public enum PinpointServerSocketStateCode {

	// 상태는 다음과 같다.
	// NONE : 아무 이벤트가 없는 상태
	// RUN : 서버로 메시지를 보내는 것만 가능한 Socket 상태 
	// RUN_DUPLEX_COMMUNICATION : 양방향 통신이 가능한 Socket 상태 (서버로 메시지를 보내는 것 서버가 보낸 메시지를 처리하는 것 )
	// BEING_SHUTDOWN : CLOSE 등의 명령을 받고 연결을 종료를 대기하는 상태 
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
