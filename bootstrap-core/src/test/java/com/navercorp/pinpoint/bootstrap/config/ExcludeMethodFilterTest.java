package com.navercorp.pinpoint.bootstrap.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExcludeMethodFilterTest {

	@Test
	public void testFilter() throws Exception {
		Filter<String> filter = new ExcludeMethodFilter("get,post");

		boolean getResult = filter.filter("GET");
		boolean postResult = filter.filter("POST");

		assertTrue(getResult);
		assertTrue(postResult);
	}

	@Test
	public void testUnFilter() throws Exception {
		Filter<String> filter = new ExcludeMethodFilter("get,post");

		boolean putResult = filter.filter("PUT");
		boolean headResult = filter.filter("HEAD");

		assertFalse(putResult);
		assertFalse(headResult);
	}
}