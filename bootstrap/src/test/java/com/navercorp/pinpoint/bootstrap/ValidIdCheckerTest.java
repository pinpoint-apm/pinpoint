package com.nhn.pinpoint.bootstrap;

import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author netspider
 * 
 */
public class ValidIdCheckerTest {

	private final Pattern p = Pattern.compile("[^a-zA-Z0-9._(\\-)]");

	@Test
	public void checkValidId() {
		Assert.assertFalse(p.matcher("PINPOINT123").find());
		Assert.assertFalse(p.matcher("P1NPOINT").find());
		Assert.assertFalse(p.matcher("1PNPOINT").find());
		Assert.assertFalse(p.matcher("P1NPOINT.DEV").find());
		Assert.assertFalse(p.matcher("P1NPOINT..DEV").find());
		Assert.assertFalse(p.matcher("P1N.POINT.DEV").find());
		Assert.assertFalse(p.matcher("P1NPOINT-DEV").find());
		Assert.assertFalse(p.matcher("P1NPOINT_DEV").find());
		Assert.assertFalse(p.matcher("P1N_POINT_DEV").find());
	}

	@Test
	public void checkInvalidId() {
		Assert.assertTrue(p.matcher("P1NPOINT가").find());
		Assert.assertTrue(p.matcher("P1NPOINT ").find());
		Assert.assertTrue(p.matcher("P1NPOINT+").find());
		Assert.assertTrue(p.matcher("PINPO+INT").find());
	}
}
