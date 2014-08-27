package com.nhn.pinpoint.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
public class PinpointServerSocketState {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private PinpointServerSocketStateCode beforeState = PinpointServerSocketStateCode.NONE;
	private PinpointServerSocketStateCode currentState = PinpointServerSocketStateCode.NONE;

	private synchronized boolean setSessionState(PinpointServerSocketStateCode state) {
		boolean enable = this.currentState.canChangeState(state);

		if (enable) {
			this.beforeState = this.currentState;
			this.currentState = state;
			return true;
		} else if (PinpointServerSocketStateCode.isFinished(this.currentState)) {
			// 상태가 더 이상 변경할수 없는 것들은 로그만 출력
			// 이미 종료 상태이기 때문에 이렇게 처리해도 큰 문제가 없음
			PinpointServerSocketStateCode checkBefore = this.beforeState;
			PinpointServerSocketStateCode checkCurrent = this.currentState;

			String errorMessage = cannotChangeMessage(checkBefore, checkCurrent, state);
			
			this.beforeState = this.currentState;
			this.currentState = PinpointServerSocketStateCode.ERROR_ILLEGAL_STATE_CHANGE;
				
			logger.warn(errorMessage);
			return false;
		} else {
			PinpointServerSocketStateCode checkBefore = this.beforeState;
			PinpointServerSocketStateCode checkCurrent = this.currentState;

			String errorMessage = errorMessage(checkBefore, checkCurrent, state);
			
			this.beforeState = this.currentState;
			this.currentState = PinpointServerSocketStateCode.ERROR_ILLEGAL_STATE_CHANGE;
				
			logger.warn(errorMessage);
			
			throw new IllegalStateException(errorMessage);
		}
	}
	
	public boolean changeStateRun() {
		return setSessionState(PinpointServerSocketStateCode.RUN);
	}
	
	public boolean changeStateRunDuplexCommunication() {
		return setSessionState(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION);
	}

	public boolean changeStateBeingShutdown() {
		return setSessionState(PinpointServerSocketStateCode.BEING_SHUTDOWN);
	}

	public boolean changeStateShutdown() {
		return setSessionState(PinpointServerSocketStateCode.SHUTDOWN);
	}

	public boolean changeStateUnexpectedShutdown() {
		return setSessionState(PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN);
	}

	public boolean changeStateUnkownError() {
		return setSessionState(PinpointServerSocketStateCode.ERROR_UNKOWN);
	}
	
	private String errorMessage(PinpointServerSocketStateCode checkBefore, PinpointServerSocketStateCode checkCurrent, PinpointServerSocketStateCode nextState) {
		return "Invalid State(current:" + checkCurrent + " before:" + checkBefore + " next:" + nextState + ")";
	}

	private String cannotChangeMessage(PinpointServerSocketStateCode checkBefore, PinpointServerSocketStateCode checkCurrent, PinpointServerSocketStateCode nextState) {
		return "Can not change State(current:" + checkCurrent + " before:" + checkBefore + " next:" + nextState + ")";
	}

	public synchronized PinpointServerSocketStateCode getCurrentState() {
		return currentState;
	}

}
