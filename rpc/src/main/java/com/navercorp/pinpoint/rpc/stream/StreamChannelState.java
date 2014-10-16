package com.nhn.pinpoint.rpc.stream;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamChannelState {

	private final AtomicReference<StreamChannelStateCode> currentStateRefernce = new AtomicReference<StreamChannelStateCode>();

	public StreamChannelState() {
		currentStateRefernce.set(StreamChannelStateCode.NEW);
	}

	public boolean changeStateOpen() {
		boolean result = currentStateRefernce.compareAndSet(StreamChannelStateCode.NEW, StreamChannelStateCode.OPEN);
		if (!result) {
			changeStateIllegal();
		}
		return result;
	}

	public boolean changeStateOpenAwait() {
		boolean result = currentStateRefernce.compareAndSet(StreamChannelStateCode.OPEN, StreamChannelStateCode.OPEN_AWAIT);
		if (!result) {
			changeStateIllegal();
		}
		return result;
	}

	public boolean changeStateOpenArrived() {
		boolean result = currentStateRefernce.compareAndSet(StreamChannelStateCode.NEW, StreamChannelStateCode.OPEN_ARRIVED);
		if (!result) {
			changeStateIllegal();
		}
		return result;
	}

	public boolean changeStateRun() {
		StreamChannelStateCode currentState = this.currentStateRefernce.get();
		
		StreamChannelStateCode nextState = StreamChannelStateCode.RUN;
		if (!nextState.canChangeState(currentState)) {
			changeStateIllegal();
			return false;
		}

		return currentStateRefernce.compareAndSet(currentState, StreamChannelStateCode.RUN);
	}

	public boolean changeStateClose() {
		if (currentStateRefernce.get() == StreamChannelStateCode.CLOSED) {
			return false;
		}
		
		currentStateRefernce.set(StreamChannelStateCode.CLOSED);
		return true;
	}
	
	private void changeStateIllegal() {
		currentStateRefernce.set(StreamChannelStateCode.ILLEGAL_STATE);
	}

	public StreamChannelStateCode getCurrentState() {
		return currentStateRefernce.get();
	}
	
}
