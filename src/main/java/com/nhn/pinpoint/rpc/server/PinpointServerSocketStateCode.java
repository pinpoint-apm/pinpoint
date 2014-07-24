package com.nhn.pinpoint.rpc.server;

import java.util.HashSet;
import java.util.Set;

/**
 * @author koo.taejin
 */
public enum PinpointServerSocketStateCode {

	// 상태는 다음과 같다.
	// NONE : 아무 이벤트가 없는 상태
	// RUN_WITHOUT_REGISTER_AGENT_RESOURCE : 동작은 하고 있지만 Agent의 정보를 서로 확인하지 못한 상태
	// RUN : Agent의 정보를 서로 확인하고 동작 중인 상태
	// BEING_SHUTDOWN : CLOSE 등의 명령을 받고 연결을 종료를 대기하는 상태 
	// SHUTDOWN : 내가 끊거나 종료대기가 되어있는 상태일때 종료  
	// UNEXPECTED_SHUTDOWN : CLOSE 등의 명령을 받지 못한 상태에서 상대방이 연결을 종료하였을떄 
	
	NONE(), 
	RUN_WITHOUT_REGISTER(NONE), 
	RUN(NONE, RUN_WITHOUT_REGISTER), 
	BEING_SHUTDOWN(RUN, RUN_WITHOUT_REGISTER),
	SHUTDOWN(RUN, RUN_WITHOUT_REGISTER, BEING_SHUTDOWN),
	UNEXPECTED_SHUTDOWN(RUN, RUN_WITHOUT_REGISTER),
	
	// 서버쪽에서 먼저 연결을 끊자는 메시지도 필요하다. 
	// 예를 들어 HELLO 이후 다 확인했는데, 같은 Agent명이 있으면(?) 이걸 사용자에게 말해야 할까? 아닐까? 알림 등
	ERROR_UNKOWN(RUN, RUN_WITHOUT_REGISTER), 
	ERROR_ILLEGAL_STATE_CHANGE(NONE, RUN, RUN_WITHOUT_REGISTER, BEING_SHUTDOWN, SHUTDOWN);

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
		if (code == RUN || code == RUN_WITHOUT_REGISTER) {
			return true;
		}

		return false;
	}

}
