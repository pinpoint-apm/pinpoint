package com.profiler.common.util;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.junit.Test;

/**
 *
 */
public class SqlParserTest {
    private SqlParser sqlParser = new SqlParser();

    @Test
    public void normalizedSql() {
        StringBuilder sb = new StringBuilder(10);

        String s = sqlParser.normalizedSql("select * from table a = 1 and b=50 and c=? and d='11'", sb);

        System.out.println(s);
        System.out.println(sb.toString());

        StringBuilder sb2 = new StringBuilder(10);
        String s2 = sqlParser.normalizedSql(" ", sb2);
        System.out.println(s2);

        System.out.println((char) -1);
        String str = "s";
        System.out.println(str.codePointAt(0));
        System.out.println((int) str.charAt(0));
        System.out.println("high" + (char) Character.MAX_HIGH_SURROGATE);
        System.out.println("low" + (char) Character.MIN_LOW_SURROGATE);

        System.out.println((int) Character.MIN_LOW_SURROGATE);
        System.out.println((int) Character.MAX_HIGH_SURROGATE);

        StringBuilder sb3 = new StringBuilder();
        String s3 = sqlParser.normalizedSql("''", sb3);
        System.out.println("s3:" + s3);
        System.out.println("sb3:" + sb3.toString());
    }

    @Test
    public void nullCheck() {
        sqlParser.normalizedSql(null, new StringBuilder());
    }

    @Test
    public void complex() {

        assertEqual("select * from table a = 1 and b=50 and c=? and d='11'",
                "select * from table a = # and b=# and c=? and d='$'", "1,50,11");

        assertEqual("select * from table a = -1 and b=-50 and c=? and d='-11'",
                "select * from table a = -# and b=-# and c=? and d='$'", "1,50,-11");

        assertEqual("select * from table a = 1/*test*/ and b=50/*test*/ and c=? and d='11'",
                "select * from table a = #/*test*/ and b=#/*test*/ and c=? and d='$'", "1,50,11");

        assertEqual("select ZIPCODE,CITY from ZIPCODE");
        assertEqual("select a.ZIPCODE,a.CITY from ZIPCODE as a");
        assertEqual("select ZIPCODE,123 from ZIPCODE", "select ZIPCODE,# from ZIPCODE", "123");

        assertEqual("SELECT * from table a=123 and b='abc' and c=1-3",
                "SELECT * from table a=# and b='$' and c=#-#", "123,abc,1,3");

        assertEqual("SYSTEM_RANGE(1, 10)", "SYSTEM_RANGE(#, #)", "1,10");

    }

    @Test
    public void etcState() {

        assertEqual("test.abc", "test.abc", "");
        assertEqual("test.abc123", "test.abc123", "");
        assertEqual("test.123", "test.123", "");

    }

    @Test
    public void objectEquals() {

        assertEqualObject("test.abc");
        assertEqualObject("test.abc123");
        assertEqualObject("test.123");

    }


    @Test
    public void numberState() {
        assertEqual("123", "#", "123");
        // -가 진짜 숫자의 -인지 알려면 구문분석이 필요하므로 그냥 숫자만 치환한다.
        assertEqual("-123", "-#", "123");
        assertEqual("+123", "+#", "123");
        assertEqual("1.23", "#", "1.23");
        assertEqual("1.23.34", "#", "1.23.34");
        assertEqual("123 456", "# #", "123,456");
        assertEqual("1.23 4.56", "# #", "1.23,4.56");
        assertEqual("1.23-4.56", "#-#", "1.23,4.56");

        assertEqual("1<2", "#<#", "1,2");
        assertEqual("1< 2", "#< #", "1,2");
        assertEqual("(1< 2)", "(#< #)", "1,2");

        assertEqual("-- 1.23", "-- 1.23", "");
        assertEqual("- -1.23", "- -#", "1.23");
        assertEqual("--1.23", "--1.23", "");
        assertEqual("/* 1.23 */", "/* 1.23 */", "");
        assertEqual("/*1.23*/", "/*1.23*/", "");
        assertEqual("/* 1.23 \n*/", "/* 1.23 \n*/", "");

        assertEqual("test123", "test123", "");
        assertEqual("test_123", "test_123", "");
        assertEqual("test_ 123", "test_ #", "123");

        // 사실 이건 불가능한 토큰임.
        assertEqual("123tst", "#tst", "123");
    }


    @Test
    public void singleLineCommentState() {
        assertEqual("--", "--", "");
        assertEqual("//", "//", "");
        assertEqual("--123", "--123", "");
        assertEqual("//123", "//123", "");
        assertEqual("--test", "--test");
        assertEqual("//test", "//test");
        assertEqual("--test\ntest", "--test\ntest", "");
        assertEqual("--test\t\n", "--test\t\n", "");
        assertEqual("--test\n123 test", "--test\n# test", "123");
    }


    @Test
    public void multiLineCommentState() {
        assertEqual("/**/", "/**/", "");
        assertEqual("/* */", "/* */", "");
        assertEqual("/* */abc", "/* */abc", "");
        assertEqual("/* * */", "/* * */", "");
        assertEqual("/* * */", "/* * */", "");

        assertEqual("/* abc", "/* abc", "");

        assertEqual("select * from table", "select * from table", "");
    }

    @Test
    public void symbolState() {
        assertEqual("''", "''", "");
        assertEqual("'abc'", "'$'", "abc");
        assertEqual("'a''bc'", "'$'", "a''bc");
        assertEqual("'a' 'bc'", "'$' '$'", "a,bc");

        assertEqual("'a''bc' 'a''bc'", "'$' '$'", "a''bc,a''bc");


        assertEqual("select * from table where a='a'", "select * from table where a='$'", "a");
    }

    //    @Test
    public void charout() {
        for (int i = 11; i < 67; i++) {
            System.out.println((char) i);
        }
    }

    @Test
    public void commentAndSymbolCombine() {
        assertEqual("/* 'test' */", "/* 'test' */", "");
        assertEqual("/* 'test'' */", "/* 'test'' */", "");
        assertEqual("/* '' */", "/* '' */");

        assertEqual("/*  */ 123 */", "/*  */ # */", "123");

        assertEqual("' /* */'", "'$'", " /* */");

    }

    private void assertEqual(String expected) {
        assertEqual(expected, expected);
    }

    private void assertEqual(String expected, String actual) {
        StringBuilder sb = new StringBuilder();
        String normalizedSql = sqlParser.normalizedSql(expected, sb);
        try {
            Assert.assertEquals(actual, normalizedSql);
        } catch (AssertionFailedError e) {
            System.err.println("Original :" + expected);
            throw e;
        }
    }

    private void assertEqual(String expected, String actual, String ouputExpected) {
        StringBuilder output = new StringBuilder();
        String normalizedSql = sqlParser.normalizedSql(expected, output);
        try {
            Assert.assertEquals("normalizedSql check", actual, normalizedSql);
        } catch (AssertionFailedError e) {
            System.err.println("Original :" + expected);
            throw e;
        }

        Assert.assertEquals("outputParam check", ouputExpected, output.toString());
    }

    private void assertEqualObject(String expected) {
        StringBuilder output = new StringBuilder();
        String normalizedSql = sqlParser.normalizedSql(expected, output);
        try {
            Assert.assertEquals("normalizedSql check", expected, normalizedSql);
            Assert.assertSame(expected, normalizedSql);
        } catch (AssertionFailedError e) {
            System.err.println("Original :" + expected);
            throw e;
        }

    }
}
