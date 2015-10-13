/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class DefaultSqlParserTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SqlParser sqlParser = new DefaultSqlParser();
    private OutputParameterParser outputParameterParser = new OutputParameterParser();

    @Test
    public void normalizedSql() {

        NormalizedSql parsingResult = sqlParser.normalizedSql("select * from table a = 1 and b=50 and c=? and d='11'");
        String s = parsingResult.getNormalizedSql();

        logger.debug(s);
        logger.debug(parsingResult.getParseParameter());

        NormalizedSql parsingResult2 = sqlParser.normalizedSql(" ");
        String s2 = parsingResult2.getNormalizedSql();
        logger.debug(s2);

        logger.debug("{}", (char) -1);
        String str = "s";
        logger.debug("{}", str.codePointAt(0));
        logger.debug("{}", (int) str.charAt(0));
        logger.debug("high:{}", Character.MAX_HIGH_SURROGATE);
        logger.debug("low:{}", Character.MIN_LOW_SURROGATE);
        logger.debug("{}", (int) Character.MIN_LOW_SURROGATE);
        logger.debug("{}", (int) Character.MAX_HIGH_SURROGATE);

        NormalizedSql parsingResult3 = sqlParser.normalizedSql("''");
        String s3 = parsingResult3.getNormalizedSql();
        logger.debug("s3:{}", s3);
        logger.debug("sb3:{}", parsingResult3.getParseParameter());
    }

    @Test
    public void nullCheck() {
        sqlParser.normalizedSql(null);
    }

    @Test
    public void complex() {

        assertEqual("select * from table a = 1 and b=50 and c=? and d='11'",
                "select * from table a = 0# and b=1# and c=? and d='2$'", "1,50,11");

        assertEqual("select * from table a = -1 and b=-50 and c=? and d='-11'",
                "select * from table a = -0# and b=-1# and c=? and d='2$'", "1,50,-11");

        assertEqual("select * from table a = +1 and b=+50 and c=? and d='+11'",
                "select * from table a = +0# and b=+1# and c=? and d='2$'", "1,50,+11");

        assertEqual("select * from table a = 1/*test*/ and b=50/*test*/ and c=? and d='11'",
                "select * from table a = 0#/*test*/ and b=1#/*test*/ and c=? and d='2$'", "1,50,11");

        assertEqual("select ZIPCODE,CITY from ZIPCODE");
        assertEqual("select a.ZIPCODE,a.CITY from ZIPCODE as a");
        assertEqual("select ZIPCODE,123 from ZIPCODE",
                "select ZIPCODE,0# from ZIPCODE", "123");

        assertEqual("SELECT * from table a=123 and b='abc' and c=1-3",
                "SELECT * from table a=0# and b='1$' and c=2#-3#", "123,abc,1,3");

        assertEqual("SYSTEM_RANGE(1, 10)",
                "SYSTEM_RANGE(0#, 1#)", "1,10");

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
        assertEqual("123", "0#", "123");
        // just converting numbers as it is too much work to find out if '-' represents a negative number, or is part of the SQL expression
        assertEqual("-123", "-0#", "123");
        assertEqual("+123", "+0#", "123");
        assertEqual("1.23", "0#", "1.23");
        assertEqual("1.23.34", "0#", "1.23.34");
        assertEqual("123 456", "0# 1#", "123,456");
        assertEqual("1.23 4.56", "0# 1#", "1.23,4.56");
        assertEqual("1.23-4.56", "0#-1#", "1.23,4.56");

        assertEqual("1<2", "0#<1#", "1,2");
        assertEqual("1< 2", "0#< 1#", "1,2");
        assertEqual("(1< 2)", "(0#< 1#)", "1,2");

        assertEqual("-- 1.23", "-- 1.23", "");
        assertEqual("- -1.23", "- -0#", "1.23");
        assertEqual("--1.23", "--1.23", "");
        assertEqual("/* 1.23 */", "/* 1.23 */", "");
        assertEqual("/*1.23*/", "/*1.23*/", "");
        assertEqual("/* 1.23 \n*/", "/* 1.23 \n*/", "");

        assertEqual("test123", "test123", "");
        assertEqual("test_123", "test_123", "");
        assertEqual("test_ 123", "test_ 0#", "123");

        // this is effectively an impossible token
        assertEqual("123tst", "0#tst", "123");
    }

    @Test
    public void numberState2() {
        assertEqual("1.23e", "0#", "1.23e");
        assertEqual("1.23E", "0#", "1.23E");
        // just converting numbers as it is too much work to find out if '-' represents a negative number, or is part of the SQL expression
        assertEqual("1.4e-10", "0#-1#", "1.4e,10");

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
        assertEqual("--test\n123 test", "--test\n0# test", "123");
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
        assertEqual("'abc'", "'0$'", "abc");
        assertEqual("'a''bc'", "'0$'", "a''bc");
        assertEqual("'a' 'bc'", "'0$' '1$'", "a,bc");

        assertEqual("'a''bc' 'a''bc'", "'0$' '1$'", "a''bc,a''bc");


        assertEqual("select * from table where a='a'", "select * from table where a='0$'", "a");
    }

    //    @Test
    public void charout() {
        for (int i = 11; i < 67; i++) {
            logger.debug("{}", (char) i);
        }
    }

    @Test
    public void commentAndSymbolCombine() {
        assertEqual("/* 'test' */", "/* 'test' */", "");
        assertEqual("/* 'test'' */", "/* 'test'' */", "");
        assertEqual("/* '' */", "/* '' */");

        assertEqual("/*  */ 123 */", "/*  */ 0# */", "123");

        assertEqual("' /* */'", "'0$'", " /* */");

    }

    @Test
    public void separatorTest() {

        assertEqual("1234 456,7", "0# 1#,2#", "1234,456,7");

        assertEqual("'1234 456,7'", "'0$'", "1234 456,,7");

        assertEqual("'1234''456,7'", "'0$'", "1234''456,,7");
        NormalizedSql parsingResult2 = this.sqlParser.normalizedSql("'1234''456,7'");
        logger.debug("{}", parsingResult2);

        // for string token
        assertEqual("'1234' '456,7'", "'0$' '1$'", "1234,456,,7");
    }


    @Test
    public void combineTest() {
        assertCombine("123 345", "0# 1#", "123,345");
        assertCombine("123 345 'test'", "0# 1# '2$'", "123,345,test");
        assertCombine("1 2 3 4 5 6 7 8 9 10 11", "0# 1# 2# 3# 4# 5# 6# 7# 8# 9# 10#", "1,2,3,4,5,6,7,8,9,10,11");
    }

    @Test
    public void combineErrorTest() {
        assertCombineErrorCase("123 10#", "0# 10#", "123,345");

        assertCombineErrorCase("1 3 10#", "0# 2# 10#", "1,2,3");

        assertCombineErrorCase("1 2 3", "0# 2 3", "1,2,3");
        assertCombineErrorCase("1 2 10", "0# 2 10", "1,2,3");
        assertCombineErrorCase("1 2 201", "0# 2 201", "1,2,3");

        assertCombineErrorCase("1 2 11", "0# 2 10#", "1,2,3,4,5,6,7,8,9,10,11");

    }

    @Test
    public void combineBindValue() {
        OutputParameterParser parameterParser = new OutputParameterParser();

        String sql = "select * from table a = 1 and b=50 and c=? and d='11'";
        List<String> bindValues = parameterParser.parseOutputParameter("foo");
        String result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);

        sql = "select * from table a = ? and b=? and c=? and d=?";
        bindValues = parameterParser.parseOutputParameter("1, 50, foo, 11");
        result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);

        sql = "select * from table id = \"foo ? bar\" and number=?";
        bindValues = parameterParser.parseOutputParameter("99");
        result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);

        sql = "select * from table id = 'hi ? name''s foo' and number=?";
        bindValues = parameterParser.parseOutputParameter("99");
        result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);

        sql = "/** comment ? */ select * from table id = ?";
        bindValues = parameterParser.parseOutputParameter("foo,,bar");
        result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);

        sql = "select /*! STRAIGHT_JOIN ? */ * from table id = ?";
        bindValues = parameterParser.parseOutputParameter("foo,,bar");
        result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);

        sql = "select * from table id = ?; -- This ? comment";
        bindValues = parameterParser.parseOutputParameter("foo");
        result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);


        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-01.sql");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sql = sb.toString();
        bindValues = parameterParser.parseOutputParameter("110,110,INT,110,INT,110,15971651,15971651,15971616,15971616,15723121,16087526,16087526,16378494,16373473,16367539,16341683,16341538,16341532,16341528,16337730,16320395,16312813,16312813,16312811,16312811,16312768,16312768,16310806,16309771,16309770,16309769,16307041,16306787,16305511,16305510,16305509,16305506,16305370,16305309,16305308,16305307,16304924,16304789,16304788,16301699,16299101,16299050,16298984,16298887,16297035,16296700,16296112,16295921,16295244,16295158,16294585,16294577,16294503,16294461,16294445,16294433,16294433,16294429,16294428,16293967,16293967,16293621,16293621,16293518,16293518,16293440,16293440,16293175,16293175,16292605,16292605,16292486,16292486,16290389,16290388,16288484,16288483,16288481,16288466,16288396,16288305,16288212,16288145,16288143,16287979,16287942,16287888,16287873,16287871,16287869,16287868,16287705,16287568,16285753,16285741,16285057,16285020,16285003,16284689,16284681,SB");
        result = sqlParser.combineBindValues(sql, bindValues);
        System.out.println(result);
    }

    private void assertCombine(String result, String sql, String outputParams) {
        List<String> output = this.outputParameterParser.parseOutputParameter(outputParams);

        NormalizedSql parsingResult = this.sqlParser.normalizedSql(result);
        Assert.assertEquals("sql", parsingResult.getNormalizedSql(), sql);
        String combine = this.sqlParser.combineOutputParams(sql, output);
        Assert.assertEquals("combine", result, combine);
    }

    private void assertCombineErrorCase(String expectedError, String sql, String outputParams) {
        List<String> output = this.outputParameterParser.parseOutputParameter(outputParams);
//        ParsingResult parsingResult = this.sqlParser.normalizedSql(result);
        String combine = this.sqlParser.combineOutputParams(sql, output);
        Assert.assertEquals("combine", expectedError, combine);
    }


    private void assertEqual(String expected) {
        NormalizedSql parsingResult = sqlParser.normalizedSql(expected);
        String normalizedSql = parsingResult.getNormalizedSql();
        try {
            Assert.assertEquals(expected, normalizedSql);
        } catch (AssertionError e) {
            logger.warn("Original :{}", expected);
            throw e;
        }
    }

    private void assertEqual(String expected, String actual) {
        NormalizedSql parsingResult = sqlParser.normalizedSql(expected);
        String normalizedSql = parsingResult.getNormalizedSql();
        try {
            Assert.assertEquals(actual, normalizedSql);
        } catch (AssertionError e) {
            logger.warn("Original :{}", expected);
            throw e;
        }
    }

    private void assertEqual(String expected, String actual, String outputExpected) {
        NormalizedSql parsingResult = sqlParser.normalizedSql(expected);
        String normalizedSql = parsingResult.getNormalizedSql();
        String output = parsingResult.getParseParameter();
        List<String> outputParams = outputParameterParser.parseOutputParameter(output);
        String s = sqlParser.combineOutputParams(normalizedSql, outputParams);
        logger.debug("combine:" + s);
        try {
            Assert.assertEquals("normalizedSql check", actual, normalizedSql);
        } catch (AssertionError e) {
            logger.warn("Original :{}", expected);
            throw e;
        }

        Assert.assertEquals("outputParam check", outputExpected, parsingResult.getParseParameter());
    }

    private void assertEqualObject(String expected) {
        NormalizedSql parsingResult = sqlParser.normalizedSql(expected);
        String normalizedSql = parsingResult.getNormalizedSql();
        try {
            Assert.assertEquals("normalizedSql check", expected, normalizedSql);
            Assert.assertSame(expected, normalizedSql);
        } catch (AssertionError e) {
            logger.warn("Original :{}", expected);
            throw e;
        }

    }
}
