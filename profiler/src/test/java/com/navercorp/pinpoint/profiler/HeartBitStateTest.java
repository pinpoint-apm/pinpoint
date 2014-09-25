package com.nhn.pinpoint.profiler;


import junit.framework.Assert;

import org.junit.Test;

public class HeartBitStateTest {

	@Test
	public void changeStateTest1() throws InterruptedException {
		HeartBitStateContext stateContext = new HeartBitStateContext();
		Assert.assertEquals(HeartBitState.NONE, stateContext.getState());

		changeStateToNeedNotRequest(stateContext);
		changeStateToFinishRequest(stateContext);
	}

	@Test
	public void changeStateTest2() throws InterruptedException {
		HeartBitStateContext stateContext = new HeartBitStateContext();
		Assert.assertEquals(HeartBitState.NONE, stateContext.getState());

		changeStateToNeedRequest(stateContext);
		changeStateToFinishRequest(stateContext);
	}

	@Test
	public void changeStateTest3() throws InterruptedException {
		HeartBitStateContext stateContext = new HeartBitStateContext();
		Assert.assertEquals(HeartBitState.NONE, stateContext.getState());

		changeStateToNeedRequest(stateContext);
		changeStateToFinishRequest(stateContext);

		Thread.sleep(1);
		boolean isSuccess = stateContext.changeStateToFinish();
		Assert.assertFalse(isSuccess);
	}
	
	@Test
	public void changeStateTest4() throws InterruptedException {
		HeartBitStateContext stateContext = new HeartBitStateContext();
		Assert.assertEquals(HeartBitState.NONE, stateContext.getState());
		
		Thread.sleep(1);
		boolean isSuccess = stateContext.changeStateToFinish();
		Assert.assertFalse(isSuccess);
	}

	private void changeStateToNeedRequest(HeartBitStateContext stateContext) throws InterruptedException {
		Thread.sleep(1);
		boolean isSuccess = stateContext.changeStateToNeedRequest(System.currentTimeMillis());

		Assert.assertTrue(isSuccess);
		Assert.assertEquals(HeartBitState.NEED_REQUEST, stateContext.getState());
	}

	private void changeStateToNeedNotRequest(HeartBitStateContext stateContext) throws InterruptedException {
		Thread.sleep(1);
		boolean isSuccess = stateContext.changeStateToNeedNotRequest(System.currentTimeMillis());

		Assert.assertTrue(isSuccess);
		Assert.assertEquals(HeartBitState.NEED_NOT_REQUEST, stateContext.getState());
	}
	
	private void changeStateToFinishRequest(HeartBitStateContext stateContext) throws InterruptedException {
		Thread.sleep(1);
		boolean isSuccess = stateContext.changeStateToFinish();

		Assert.assertTrue(isSuccess);
		Assert.assertEquals(HeartBitState.FINISH, stateContext.getState());
	}
	
}
