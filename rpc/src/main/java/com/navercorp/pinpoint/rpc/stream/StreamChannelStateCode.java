package com.nhn.pinpoint.rpc.stream;

import java.util.HashSet;
import java.util.Set;

/**
 * @author koo.taejin <kr14910>
 */
public enum StreamChannelStateCode {

	NEW, 
	OPEN(NEW), 
	OPEN_AWAIT(OPEN), 
	OPEN_ARRIVED(OPEN), 
	RUN(OPEN_AWAIT, OPEN_ARRIVED), 
	CLOSED(OPEN_AWAIT, OPEN_ARRIVED, RUN), 
	ILLEGAL_STATE(NEW, OPEN, OPEN_AWAIT, OPEN_ARRIVED, RUN, CLOSED);

	private final Set<StreamChannelStateCode> validBeforeStateSet;

	private StreamChannelStateCode(StreamChannelStateCode... validBeforeStates) {
		this.validBeforeStateSet = new HashSet<StreamChannelStateCode>();

		if (validBeforeStates != null) {
			for (StreamChannelStateCode eachStateCode : validBeforeStates) {
				validBeforeStateSet.add(eachStateCode);
			}
		}
	}

	public boolean canChangeState(StreamChannelStateCode currentState) {
		if (validBeforeStateSet.contains(currentState)) {
			return true;
		}

		return false;
	}

}
