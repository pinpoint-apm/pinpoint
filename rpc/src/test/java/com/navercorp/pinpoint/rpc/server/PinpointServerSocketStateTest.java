package com.nhn.pinpoint.rpc.server;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author koo.taejin
 */
public class PinpointServerSocketStateTest {

	// Agent버전이 최신일 경우 (2014-07 기준)
	// 가장 기본적인 형태 RUN -> 이후 Agent 정보 획득 RUN_DUPLEX_COMMUNICATION ->  클라이언트 종료전 Agent 정보 제거 요청에 따른 BEING_SHUTDOWN -> 연결종료
	@Test
	public void changeStateTest1() {
		PinpointServerSocketState state = new PinpointServerSocketState();

		state.changeStateRun();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN, state.getCurrentState());

		state.changeStateRunDuplexCommunication();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, state.getCurrentState());

		state.changeStateBeingShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.BEING_SHUTDOWN, state.getCurrentState());

		state.changeStateShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.SHUTDOWN, state.getCurrentState());
	}

	// Agent버전이 최신일 경우 (2014-07 기준)
	// 가장 기본적인 형태 RUN_DUPLEX_COMMUNICATION -> 이후 Agent 정보 획득 RUN_DUPLEX_COMMUNICATION ->  클라이언트 종료전 Agent 정보 제거 요청에 따른 BEING_SHUTDOWN -> 연결종료
	@Test
	public void changeStateTest2() {
		PinpointServerSocketState state = new PinpointServerSocketState();

		state.changeStateRunDuplexCommunication();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, state.getCurrentState());

		state.changeStateBeingShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.BEING_SHUTDOWN, state.getCurrentState());

		state.changeStateShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.SHUTDOWN, state.getCurrentState());
	}

	// Agent버전이 구버전일 경우 (2014-07 기준)
	// 가장 기본적인 형태 RUN -> 연결종료
	@Test
	public void changeStateTest3() {
		PinpointServerSocketState state = new PinpointServerSocketState();

		state.changeStateRun();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN, state.getCurrentState());

		state.changeStateUnexpectedShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.UNEXPECTED_SHUTDOWN, state.getCurrentState());
	}

	// Agent버전이 구버전일 경우 (2014-07 기준)
	// 가장 기본적인 형태 RUN -> 연결종료
	@Test
	public void changeStateTest4() {
		PinpointServerSocketState state = new PinpointServerSocketState();

		state.changeStateRun();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN, state.getCurrentState());

		state.changeStateShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.SHUTDOWN, state.getCurrentState());
	}

	@Test
	public void changeStateTest5() {
		PinpointServerSocketState state = new PinpointServerSocketState();

		state.changeStateRunDuplexCommunication();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, state.getCurrentState());

		state.changeStateShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.SHUTDOWN, state.getCurrentState());
	}
	
	

	@Test(expected = IllegalStateException.class)
	public void invalidChangeStateTest1() {
		PinpointServerSocketState state = new PinpointServerSocketState();

		state.changeStateBeingShutdown();
	}

	@Test(expected = IllegalStateException.class)
	public void invalidChangeStateTest2() {
		PinpointServerSocketState state = new PinpointServerSocketState();

		state.changeStateRunDuplexCommunication();
		Assert.assertEquals(PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION, state.getCurrentState());

		state.changeStateBeingShutdown();
		Assert.assertEquals(PinpointServerSocketStateCode.BEING_SHUTDOWN, state.getCurrentState());

		state.changeStateUnexpectedShutdown();
	}

}
