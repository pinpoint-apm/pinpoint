package com.nhn.hippo.web.calltree.server;

import org.junit.Test;

public class HistogramTest {

	@Test
	public void testHistogram() {
		Histogram histogram1 = new Histogram(100);
		for (int i = 0; i < 10; i++) {
			histogram1.addSample(i);
		}
		
		Histogram histogram2 = new Histogram(100);
		histogram2.addSample(5);
		for (int i = 200; i < 400; i++) {
			histogram2.addSample(i);
		}

		System.out.println(histogram1);
		System.out.println(histogram2);
		System.out.println(histogram1.mergeWith(histogram2));
		System.out.println(histogram1.getMin());
		System.out.println(histogram1.getMax());
	}

}
