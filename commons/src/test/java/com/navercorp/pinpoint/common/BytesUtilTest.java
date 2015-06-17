package com.navercorp.pinpoint.common;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.common.util.BytesUtils;

public class BytesUtilTest {

	@Test
	public void testAppropriateWriteBytes() {
		byte[] dst = new byte[10];
		byte[] src = new byte[5];
		src[0] = 1;
		src[1] = 2;
		src[2] = 3;
		src[3] = 4;
		src[4] = 5;
		//proper return?
		Assert.assertEquals(3, BytesUtils.writeBytes(dst, 1, src, 2, 2));
		//successful write?
		Assert.assertEquals(3, dst[1]);
		Assert.assertEquals(4, dst[2]);
	}
	
	@Test
	public void testOverflowDestinationWriteBytes() {
		byte[] dst = new byte[5];
		byte[] src = new byte[10];
		for(int i=0;i<10;i++) {
			src[i]=(byte)(i+1);
		}
		try {
			//overflow!
			BytesUtils.writeBytes(dst, 0, src);
			//if it does not catch any errors, it means memory leak!
			fail("invalid memory access");
		} catch(Exception e) {
			//nice
		}
	}
	
	@Test
	public void testAppropriateBytesToLong() {
		byte[] such_long = new byte[12];
		int i;
		for(i=0;i<12;i++)
		{
			such_long[i] = (byte)((i<<4) + i); 
		}
		Assert.assertEquals(0x33445566778899AAl, BytesUtils.bytesToLong(such_long, 3));
	}
	
	@Test
	public void testOverflowBytesToLong() {
		byte[] such_long = new byte[12];
		int i;
		for(i=0;i<12;i++)
		{
			such_long[i] = (byte)((i<<4) + i); 
		}
		try {
			//overflow!
			BytesUtils.bytesToLong(such_long, 9);
			//if it does not catch any errors, it means memory leak!
			fail("invalid memory access");
		} catch(Exception e) {
			//nice
		}
	}
	
	@Test
	public void testWriteLong() {
		try {
			BytesUtils.writeLong(1234, null, 0);
			fail("null pointer accessed");
		} catch (Exception e) {
			
		}
		byte[] such_long = new byte[13];
		try {
			BytesUtils.writeLong(1234, such_long, -1);
			fail("negative offset did not catched");
		} catch (Exception e) {
			
		}
		try {
			BytesUtils.writeLong(2222, such_long, 9);
			fail("index out of range exception did not catched");
		} catch (Exception e) {
			
		}
		BytesUtils.writeLong(-1l, such_long, 2);
		for(int i=2;i<10;i++) {
			Assert.assertEquals((byte)0xFF, such_long[i]);
		}
	}
	
	@Test
	public void testTrimRight() {
		String testStr = new String();
		//no space
		testStr = "Shout-out! EE!";
		Assert.assertEquals("Shout-out! EE!", BytesUtils.trimRight(testStr));
		//right spaced
		testStr = "Shout-out! YeeYee!       ";
		Assert.assertEquals("Shout-out! YeeYee!", BytesUtils.trimRight(testStr));
	}

	@Test
	public void testByteTrimRight() {
		String testStr = new String();
		//no space
		testStr = "Shout-out! EE!";
		byte[] testByte1 = new byte[testStr.length()];
		for(int i=0;i<testByte1.length;i++) {
			testByte1[i] = (byte)testStr.charAt(i);
		}
		Assert.assertEquals("out-out!", BytesUtils.toStringAndRightTrim(testByte1, 2, 9));
		//right spaced
		testStr = "Shout-out! YeeYee!       ";
		byte[] testByte2 = new byte[testStr.length()];
		for(int i=0;i<testByte2.length;i++) {
			testByte2[i] = (byte)testStr.charAt(i);
		}
		Assert.assertEquals(" YeeYee!", BytesUtils.toStringAndRightTrim(testByte2, 10, 10));
	}
}
