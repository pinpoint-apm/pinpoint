package com.navercorp.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Test;

public class ArrayUtilsTest {

	@Test
	public void testDropToString() {
		//null test
		Assert.assertTrue(ArrayUtils.dropToString(null).equals("null"));
		//zero-sized array test
		byte[] bytes_zero = new byte[0];
		Assert.assertEquals("[]", ArrayUtils.dropToString(bytes_zero));
		//small buffer with default limit
		byte[] bytes_short = new byte[4];
		for(int i=0;i<4;i++)
		{
			bytes_short[i] = 'A';
		}
		Assert.assertEquals("[65, 65, 65, 65]", ArrayUtils.dropToString(bytes_short));
		//big buffer with small limit
		byte[] bytes = new byte[256];
		for(int i=0;i<4;i++)
		{
			bytes[i] = 'A';
		}
		for(int i=4;i<256;i++)
		{
			bytes[i] = 'B';
		}
		String answer = new String("[");
		for(int i=0;i<4;i++)
		{
			answer = answer + "65, ";
		}
		for(int i=4;i<16;i++)
		{
			answer = answer + "66, ";
		}
		answer = answer + "...(240)]";
		Assert.assertEquals(answer, ArrayUtils.dropToString(bytes, 16));
		//big buffer with big limit
		answer = "[";
		for(int i=0;i<4;i++)
		{
			answer = answer + "65, ";
		}
		for(int i=4;i<255;i++)
		{
			answer = answer + "66, ";
		}
		answer = answer + "66]";
		Assert.assertEquals(answer, ArrayUtils.dropToString(bytes, 256));
		//big buffer with default limit
		answer = "[";
		for(int i=0;i<4;i++)
		{
			answer = answer + "65, ";
		}
		for(int i=4;i<32;i++)
		{
			answer = answer + "66, ";
		}
		answer = answer + "...(224)]";
		Assert.assertEquals(answer, ArrayUtils.dropToString(bytes));
	}

}
