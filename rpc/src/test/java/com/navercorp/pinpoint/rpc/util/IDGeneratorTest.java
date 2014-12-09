package com.navercorp.pinpoint.rpc.util;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.rpc.util.IDGenerator;

public class IDGeneratorTest {

	@Test
	public void generatorTest() {
		IDGenerator generator = new IDGenerator();
		
		Assert.assertEquals(1, generator.generate());
		Assert.assertEquals(2, generator.generate());
		Assert.assertEquals(3, generator.generate());
		
		generator = new IDGenerator(2, 3);
		
		Assert.assertEquals(2, generator.generate());
		Assert.assertEquals(5, generator.generate());
		Assert.assertEquals(8, generator.generate());
	}
	
}
