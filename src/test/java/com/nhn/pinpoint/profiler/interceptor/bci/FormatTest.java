package com.nhn.pinpoint.profiler.interceptor.bci;

import org.junit.Test;

import java.text.Format;
import java.util.Formatter;

/**
 * @author emeroad
 */
public class FormatTest {
    @Test
    public void format() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        formatter.format("interceptor.after(%1s)", "tsest");

        formatter.format("interceptor.afteddddr(%1s)", "tsest", "dd");
        System.out.println();
    }
    @Test
    public void format2() {
        StringBuilder sb = new StringBuilder();
        sb.append("dddd");
        Formatter formatter = new Formatter(sb);

        formatter.format("interceptor.afteddddr(%s, %s, %s)", 16, 34234, 333);
        System.out.println(sb.toString());
    }
}
