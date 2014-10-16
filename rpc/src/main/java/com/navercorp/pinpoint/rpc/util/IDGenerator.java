package com.nhn.pinpoint.rpc.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author koo.taejin <kr14910>
 */
public class IDGenerator {

	private final AtomicInteger idGenerator;

	private final int startIndex;
	private final int gap;

	public IDGenerator() {
		this(1, 1);
	}

	public IDGenerator(int startIndex) {
		this(startIndex, 1);
	}

	public IDGenerator(int startIndex, int gap) {
		AssertUtils.assertTrue(startIndex >= 0, "Startindex must be grater than or equal to 0.");
		AssertUtils.assertTrue(gap > 0, "Gap must be grater than 0.");

		this.startIndex = startIndex;
		this.gap = gap;

		idGenerator = new AtomicInteger(startIndex);
	}

	public int generate() {
		return idGenerator.getAndAdd(gap);
	}
	
	public int get() {
		return idGenerator.get();
	}

	public static IDGenerator createOddIdGenerator() {
		return new IDGenerator(1, 2);
	}

	public static IDGenerator createEvenIdGenerator() {
		return new IDGenerator(2, 2);
	}

}
