package com.nhn.pinpoint.bootstrap.config;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExcludeUrlFilterTest {

	@Test
	public void testFilter() throws Exception {
   		Filter<String> filter = new ExcludeUrlFilter("/monitor/l7check.html, test/l4check.html");

		assertFilter(filter);
	}


	@Test
	public void testFilter_InvalidExcludeURL() throws Exception {
		Filter<String> filter = new ExcludeUrlFilter("/monitor/l7check.html, test/l4check.html, ,,");

		assertFilter(filter);
	}

	@Test
	public void testFilter_emptyExcludeURL() throws Exception {
		Filter<String> filter = new ExcludeUrlFilter("");

		Assert.assertFalse(filter.filter("/monitor/l7check.html"));
		Assert.assertFalse(filter.filter("test/l4check.html"));

		Assert.assertFalse(filter.filter("test/"));
		Assert.assertFalse(filter.filter("test/l4check.htm"));
	}


	private void assertFilter(Filter<String> filter) {
		Assert.assertTrue(filter.filter("/monitor/l7check.html"));
		Assert.assertTrue(filter.filter("test/l4check.html"));

		Assert.assertFalse(filter.filter("test/"));
		Assert.assertFalse(filter.filter("test/l4check.htm"));
	}



}