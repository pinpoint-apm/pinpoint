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

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.rpc.stream.StreamChannelState;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateCode;

public class StreamChannelStateTest {

    @Te    t
	public void functionTest       () {
		StreamChannelState state = new StreamChan       elState();
		Assert.assertEquals(StreamChannelStateCode.NEW, state.ge          CurrentState());
	
       	state.changeStateOpen();
		Assert.assertEquals(StreamChannelStateCode.       PEN, state.getCurrentStat       ());

		state.changeStateOpenAwait();
		Assert.assertEquals(StreamChannelSta             eCode.OPEN_AWAIT        state.getCurrentState());
		
		state.changeStateRun();
		Assert.asse             tEquals(StreamChan       elStateCode.RUN, state.getCurrentState());
		
		state.changeStateClose()
	    Assert.assertEquals(StreamC       annelStateCode.CLOSED, state.getCurrentState());       	}
	
	@Test
	public void functionTest2() {
		StreamChannelState state          = new StreamChannelState()
		Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());
	
       	state.changeStateO       enArrived();
		Assert.assertEquals(StreamChannelStateCode.OPEN_ARRIVE             , state.getCurrent       tate());

		state.changeStateRun();
		Assert.assertEquals(StreamChannelS        teC    de.RUN, state.getCurrentSta       e());
		
		state.changeStateClose();
		Assert.as       ertEquals(StreamChannelStateCode.CLOSED, state.getCurrentState());
	}
	@Test
	public void functionTest3(        {
		StreamChannelState       state = new StreamChannelState();
		Assert.assertEquals(StreamChannelStateCode.          EW     state.getCurrentState());

		boolean result = state.changeStateRun();
		As       ert.assertFalse(result);
		Assert.assertEquals(StreamChannelStateCode          ILLEGAL_STATE, stat       .getCurrentState());
	}
	
	@Test
	public void functionTest4() {
		Stre             mChannelState state = new StreamC       annelState();
		Assert.       ssertEquals(StreamChannelStateCode.NEW, state.getCurrentState());
	
		state.cha    geStateOpen();
		Assert.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());
		
		boolean result = state.changeStateRun();
		Assert.assertFalse(result);
		Assert.assertEquals(StreamChannelStateCode.ILLEGAL_STATE, state.getCurrentState());
	}

}
