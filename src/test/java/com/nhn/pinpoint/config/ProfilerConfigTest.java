package com.nhn.pinpoint.config;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class ProfilerConfigTest {

	@Test
	public void testIsProfilableClassWithNoConfiguration() throws IOException {
		ProfilerConfig profilerConfig = new ProfilerConfig();
//		profilerConfig.readConfigFile();
		
		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/hippo/testweb/controllers/MyController"));
		Assert.assertFalse(profilerConfig.isProfilableClass("net/spider/king/wang/Jjang"));
		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/hippo/testweb2/controller/MyController"));
		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/hippo/testweb2/MyClass"));
	}
	
	/**
	 * <pre>
	 * configuration is 
	 * profile.package.include=com.nhn.hippo.testweb.controller.*,com.nhn.hippo.testweb.MyClass
	 * </pre>
	 * 
	 * @throws IOException
	 */
	@Test
	public void testIsProfilableClass() throws IOException {
		System.setProperty("hippo.config", "src/test/resources/hippo.config");
		ProfilerConfig profilerConfig = new ProfilerConfig();
//		profilerConfig.readConfigFile();

		Assert.assertTrue(profilerConfig.isProfilableClass("com/nhn/hippo/testweb/MyClass"));
		Assert.assertTrue(profilerConfig.isProfilableClass("com/nhn/hippo/testweb/controller/MyController"));
		Assert.assertTrue(profilerConfig.isProfilableClass("com/nhn/hippo/testweb/controller/customcontroller/MyCustomController"));

		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/hippo/testweb/MyUnknownClass"));
		Assert.assertFalse(profilerConfig.isProfilableClass("com/nhn/hippo/testweb/controller2/MyController"));
	}

}
