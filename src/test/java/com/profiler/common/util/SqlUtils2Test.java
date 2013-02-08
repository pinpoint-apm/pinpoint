package com.profiler.common.util;

import org.junit.Test;

/**
 *
 */
public class SqlUtils2Test {
    @Test
    public void testNormalizedSql() throws Exception {
        StringBuilder sb = new StringBuilder();
        String s = SqlUtils2.normalizedSql0("test.123", sb, false);
        System.out.println(s);

        String s2 = SqlUtils2.normalizedSql0("(123)", sb, false);

    }

    @Test
    public void testNormalizedSql2() throws Exception {
        StringBuilder sb = new StringBuilder();
        String s = SqlUtils2.normalizedSql0("(123,345)", sb, false);
        System.out.println(s);
        System.out.println(sb);

    }

    @Test
    public void testNormalizedSql3() throws Exception {
        StringBuilder sb = new StringBuilder();
        String s = SqlUtils2.normalizedSql0("'123,456'", sb, false);
        System.out.println(s);
        System.out.println(sb);

    }

    public void test() {

    }
}
