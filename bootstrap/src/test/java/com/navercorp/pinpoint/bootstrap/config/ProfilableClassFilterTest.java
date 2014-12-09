package com.navercorp.pinpoint.bootstrap.config;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.config.ProfilableClassFilter;

import java.io.IOException;

public class ProfilableClassFilterTest {

	@Test
	public void testIsProfilableClassWithNoConfiguration() throws IOException {
		ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.MyClass");

		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/controllers/MyController"));
		Assert.assertFalse(filter.filter("net/spider/king/wang/Jjang"));
		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb2/controller/MyController"));
		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb2/MyClass"));
	}

	/**
	 * <pre>
	 * configuration is
	 * profile.package.include=com.navercorp.pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.MyClass
	 * </pre>
	 *
	 * @throws IOException
	 */
	@Test
	public void testIsProfilableClass() throws IOException {
		ProfilableClassFilter filter = new ProfilableClassFilter("com.navercorp.pinpoint.testweb.controller.*,com.navercorp.pinpoint.testweb.MyClass");

		Assert.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/MyClass"));
		Assert.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/controller/MyController"));
		Assert.assertTrue(filter.filter("com/navercorp/pinpoint/testweb/controller/customcontroller/MyCustomController"));

		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/MyUnknownClass"));
		Assert.assertFalse(filter.filter("com/navercorp/pinpoint/testweb/controller2/MyController"));
	}

}