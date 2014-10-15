package com.nhn.pinpoint.bootstrap.config;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;

public class ProfilableClassFilterTest {

	@Test
	public void testIsProfilableClassWithNoConfiguration() throws IOException {
		ProfilableClassFilter filter = new ProfilableClassFilter("com.nhn.pinpoint.testweb.controller.*,com.nhn.pinpoint.testweb.MyClass");

		Assert.assertFalse(filter.filter("com/nhn/pinpoint/testweb/controllers/MyController"));
		Assert.assertFalse(filter.filter("net/spider/king/wang/Jjang"));
		Assert.assertFalse(filter.filter("com/nhn/pinpoint/testweb2/controller/MyController"));
		Assert.assertFalse(filter.filter("com/nhn/pinpoint/testweb2/MyClass"));
	}

	/**
	 * <pre>
	 * configuration is
	 * profile.package.include=com.nhn.pinpoint.testweb.controller.*,com.nhn.pinpoint.testweb.MyClass
	 * </pre>
	 *
	 * @throws IOException
	 */
	@Test
	public void testIsProfilableClass() throws IOException {
		ProfilableClassFilter filter = new ProfilableClassFilter("com.nhn.pinpoint.testweb.controller.*,com.nhn.pinpoint.testweb.MyClass");

		Assert.assertTrue(filter.filter("com/nhn/pinpoint/testweb/MyClass"));
		Assert.assertTrue(filter.filter("com/nhn/pinpoint/testweb/controller/MyController"));
		Assert.assertTrue(filter.filter("com/nhn/pinpoint/testweb/controller/customcontroller/MyCustomController"));

		Assert.assertFalse(filter.filter("com/nhn/pinpoint/testweb/MyUnknownClass"));
		Assert.assertFalse(filter.filter("com/nhn/pinpoint/testweb/controller2/MyController"));
	}

}