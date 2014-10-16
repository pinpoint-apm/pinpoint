package com.nhn.pinpoint.rpc.stream;

import junit.framework.Assert;

import org.junit.Test;

public class StreamChannelStateTest {

	@Test
	public void functionTest1() {
		StreamChannelState state = new StreamChannelState();
		Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());
	
		state.changeStateOpen();
		Assert.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());

		state.changeStateOpenAwait();
		Assert.assertEquals(StreamChannelStateCode.OPEN_AWAIT, state.getCurrentState());
		
		state.changeStateRun();
		Assert.assertEquals(StreamChannelStateCode.RUN, state.getCurrentState());
		
		state.changeStateClose();
		Assert.assertEquals(StreamChannelStateCode.CLOSED, state.getCurrentState());
	}
	
	@Test
	public void functionTest2() {
		StreamChannelState state = new StreamChannelState();
		Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());
	
		state.changeStateOpenArrived();
		Assert.assertEquals(StreamChannelStateCode.OPEN_ARRIVED, state.getCurrentState());

		state.changeStateRun();
		Assert.assertEquals(StreamChannelStateCode.RUN, state.getCurrentState());
		
		state.changeStateClose();
		Assert.assertEquals(StreamChannelStateCode.CLOSED, state.getCurrentState());
	}

	@Test
	public void functionTest3() {
		StreamChannelState state = new StreamChannelState();
		Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());
	
		boolean result = state.changeStateRun();
		Assert.assertFalse(result);
		Assert.assertEquals(StreamChannelStateCode.ILLEGAL_STATE, state.getCurrentState());
	}
	
	@Test
	public void functionTest4() {
		StreamChannelState state = new StreamChannelState();
		Assert.assertEquals(StreamChannelStateCode.NEW, state.getCurrentState());
	
		state.changeStateOpen();
		Assert.assertEquals(StreamChannelStateCode.OPEN, state.getCurrentState());
		
		boolean result = state.changeStateRun();
		Assert.assertFalse(result);
		Assert.assertEquals(StreamChannelStateCode.ILLEGAL_STATE, state.getCurrentState());
	}

}
