package com.nhn.pinpoint.thrift.io;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author koo.taejin
 */
public class TCommandTypeVersionTest {

	@Test
	public void versionTest1() {
		TCommandTypeVersion version = TCommandTypeVersion.V_1_0_2_SNAPSHOT;
		
		List<TCommandType> supportTypeList = version.getSupportCommandList();

		Assert.assertEquals(2, supportTypeList.size());
		Assert.assertTrue(supportTypeList.contains(TCommandType.THREAD_DUMP));
		Assert.assertTrue(supportTypeList.contains(TCommandType.RESULT));
	}

	@Test
	public void versionTest2() {
		TCommandTypeVersion version = TCommandTypeVersion.UNKNOWN;
		
		List<TCommandType> supportTypeList = version.getSupportCommandList();

		Assert.assertEquals(0, supportTypeList.size());
	}
	
	@Test
	public void versionTest3() {
		TCommandTypeVersion version = TCommandTypeVersion.getVersion("1.0.0");
		Assert.assertEquals(TCommandTypeVersion.UNKNOWN, version);
		
		version = TCommandTypeVersion.getVersion(TCommandTypeVersion.V_1_0_2_SNAPSHOT.getVersionName());
		Assert.assertEquals(TCommandTypeVersion.V_1_0_2_SNAPSHOT, version);
	}
	
	
}
