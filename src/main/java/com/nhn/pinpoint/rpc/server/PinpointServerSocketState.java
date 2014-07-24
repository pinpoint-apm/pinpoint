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

	private synchronized void setSessionState(PinpointServerSocketStateCode state) {
		boolean enable = this.currentState.canChangeState(state);

		if (enable) {
			this.beforeState = this.currentState;
			this.currentState = state;
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
	
	public void changeStateRun() {
		setSessionState(PinpointServerSocketStateCode.RUN);
	}

	public void changeStateRunWithoutRegister() {
		setSessionState(PinpointServerSocketStateCode.RUN_WITHOUT_REGISTER);
	}
	
	public void changeStateBeingShutdown() {
		setSessionState(PinpointServerSocketStateCode.BEING_SHUTDOWN);
	}

	public void changeStateShutdown() {
		setSessionState(PinpointServerSocketStateCode.SHUTDOWN);
	}

	public void changeStateUnexpectedShutdown() {
		setSessionState(PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN);
	}

	public void changeStateUnkownError() {
		setSessionState(PinpointServerSocketStateCode.ERROR_UNKOWN);
	}
	
	private String errorMessage(PinpointServerSocketStateCode checkBefore, PinpointServerSocketStateCode checkCurrent, PinpointServerSocketStateCode nextState) {
		return "Invalid State(current:" + checkCurrent + " before:" + checkBefore + " next:" + nextState;
	}

	public PinpointServerSocketStateCode getCurrentState() {
		return currentState;
	}

}
