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

package com.navercorp.pinpoint.rpc.stream;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author koo.taejin
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
