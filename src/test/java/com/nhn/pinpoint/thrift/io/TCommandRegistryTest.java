package com.nhn.pinpoint.thrift.io;

import junit.framework.Assert;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Test;

import com.nhn.pinpoint.thrift.dto.TResult;
import com.nhn.pinpoint.thrift.dto.command.TCommandThreadDump;

/**
 * @author koo.taejin
 */
public class TCommandRegistryTest {

	@Test
	public void registryTest1() {
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeVersion.UNKNOWN);

		Assert.assertFalse(registry.isSupport(TCommandType.RESULT.getType()));
		Assert.assertFalse(registry.isSupport(TCommandType.THREAD_DUMP.getType()));

		Assert.assertFalse(registry.isSupport(TResult.class));
		Assert.assertFalse(registry.isSupport(TCommandThreadDump.class));
	}

	@Test(expected = TException.class)
	public void registryTest2() throws TException {
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeVersion.UNKNOWN);

		registry.headerLookup(new TResult());
	}

	@Test(expected = TException.class)
	public void registryTest3() throws TException {
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeVersion.UNKNOWN);

		registry.tBaseLookup(TCommandType.RESULT.getType());
	}

	@Test
	public void registryTest4() {
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

		Assert.assertTrue(registry.isSupport(TCommandType.RESULT.getType()));
		Assert.assertTrue(registry.isSupport(TCommandType.THREAD_DUMP.getType()));

		Assert.assertTrue(registry.isSupport(TResult.class));
		Assert.assertTrue(registry.isSupport(TCommandThreadDump.class));
	}
	
	@Test
	public void registryTest5() throws TException {
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

		Header header = registry.headerLookup(new TResult());
		Assert.assertNotNull(header);
	}
	
	@Test
	public void registryTest6() throws TException {
		TCommandRegistry registry = new TCommandRegistry(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

		TBase tBase = registry.tBaseLookup(TCommandType.RESULT.getType());
		Assert.assertEquals(tBase.getClass(), TResult.class);
		
		tBase = registry.tBaseLookup(TCommandType.THREAD_DUMP.getType());
		Assert.assertEquals(tBase.getClass(), TCommandThreadDump.class);
	}

}
